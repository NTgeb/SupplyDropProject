package com.example.supplydrop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SingleRecipientActivity extends AppCompatActivity {

    TextView tvRecipientTitle, tvDescription, tvCategory, tvQuantityNeeded;
    EditText tvAmount;
    Button btnProfile, btnIncrease, btnDecrease, btnDonate;
    ImageView imgRecipient;
    ListView lvSimilarOpportunities;

    int donationAmount = 1;
    int availableQty = 0;
    int donorId;
    String requestId, recipientId, catName;

    OkHttpClient client = new OkHttpClient();
    String baseUrl = "https://wmc.ms.wits.ac.za/students/sgroup2711/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_recipient);

        tvRecipientTitle       = findViewById(R.id.tvRecipientTitle);
        tvDescription          = findViewById(R.id.tvDescription);
        tvCategory             = findViewById(R.id.tvCategory);
        tvAmount               = findViewById(R.id.tvAmount);
        tvQuantityNeeded       = findViewById(R.id.tvQuantityNeeded);
        btnProfile             = findViewById(R.id.btnProfile);
        btnIncrease            = findViewById(R.id.btnIncrease);
        btnDecrease            = findViewById(R.id.btnDecrease);
        btnDonate              = findViewById(R.id.btnDonate);
        imgRecipient           = findViewById(R.id.imgRecipient);
        lvSimilarOpportunities = findViewById(R.id.lvSimilarOpportunities);

        SharedPreferences prefs = getSharedPreferences(
                "SupplyDropPrefs", MODE_PRIVATE);
        donorId = prefs.getInt("donor_id", -1);

        requestId   = getIntent().getStringExtra("request_id");
        recipientId = getIntent().getStringExtra("recipient_id");

        tvAmount.setText(String.valueOf(donationAmount));

        // Watches every keystroke in the amount field.
        // The instant the typed number exceeds availableQty
        // the donate button goes grey and disabled.
        // The instant it drops back to a valid number
        // the donate button goes yellow and enabled again.
        tvAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                String currentText = s.toString().trim();
                if (currentText.isEmpty()) {
                    donationAmount = 1;
                } else {
                    try {
                        donationAmount = Integer.parseInt(currentText);
                    } catch (NumberFormatException e) {
                        donationAmount = 1;
                    }
                }
                updateDonateButton();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnDecrease.setOnClickListener(v -> {
            String currentText = tvAmount.getText().toString().trim();
            if (!currentText.isEmpty()) {
                donationAmount = Integer.parseInt(currentText);
            }
            if (donationAmount > 1) {
                donationAmount--;
                tvAmount.setText(String.valueOf(donationAmount));
                updateDonateButton();
            } else {
                Toast.makeText(this, "Minimum donation is 1",
                        Toast.LENGTH_SHORT).show();
            }
        });

        tvAmount.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String currentText = tvAmount.getText().toString().trim();
                try {
                    int typed = currentText.isEmpty() ? 0
                            : Integer.parseInt(currentText);
                    if (typed < 1) {
                        donationAmount = 1;
                        tvAmount.setText(String.valueOf(1));
                        Toast.makeText(SingleRecipientActivity.this,
                                "Minimum donation is 1",
                                Toast.LENGTH_SHORT).show();
                    } else if (availableQty > 0 && typed > availableQty) {
                        donationAmount = availableQty;
                        tvAmount.setText(String.valueOf(availableQty));
                        Toast.makeText(SingleRecipientActivity.this,
                                "Maximum available is " + availableQty,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        donationAmount = typed;
                    }
                } catch (NumberFormatException e) {
                    donationAmount = 1;
                    tvAmount.setText(String.valueOf(1));
                }
                updateDonateButton();
            }
        });

        fetchRequestDetails();
    }

    // Checks donationAmount against availableQty.
    // If amount exceeds available or available is 0:
    //   - donate button disabled and greyed out
    // If amount is valid:
    //   - donate button enabled and yellow
    private void updateDonateButton() {
        if (availableQty <= 0 || donationAmount > availableQty) {
            btnDonate.setEnabled(false);
            btnDonate.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.GRAY));
        } else {
            btnDonate.setEnabled(true);
            btnDonate.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            ContextCompat.getColor(
                                    SingleRecipientActivity.this,
                                    R.color.yellow)));
        }
    }

    private void fetchRequestDetails() {
        RequestBody requestBody = new FormBody.Builder()
                .add("request_id", requestId)
                .build();

        Request request = new Request.Builder()
                .url(baseUrl + "get_single_request.php")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call,
                                  @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(SingleRecipientActivity.this,
                                "Failed to load request",
                                Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call,
                                   @NonNull Response response)
                    throws IOException {
                final String body = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(body);
                        if (obj.getBoolean("success")) {
                            String itemName    = obj.getString("item_name");
                            String description = obj.optString("description", "");
                            String quantity    = obj.getString("quantity");
                            String fullName    = obj.getString("full_name");
                            catName            = obj.getString("cat_name");
                            String catId       = obj.getString("cat_id");
                            String itemImage   = obj.optString("item_image", "");

                            tvRecipientTitle.setText(itemName + " — " + fullName);
                            tvDescription.setText(description.isEmpty()
                                    ? "No description yet" : description);
                            tvCategory.setText("Category: " + catName);
                            tvQuantityNeeded.setText(quantity + " remaining");

                            if (!itemImage.isEmpty()
                                    && !itemImage.equals("null")) {
                                Glide.with(SingleRecipientActivity.this)
                                        .load(itemImage)
                                        .placeholder(R.drawable.account_circle)
                                        .into(imgRecipient);
                            }

                            int qty = Integer.parseInt(quantity);
                            availableQty = qty;

                            // Now that availableQty is set, update the
                            // button to its correct initial state
                            updateDonateButton();

                            if (qty <= 0) {
                                btnIncrease.setEnabled(false);
                            } else {
                                btnIncrease.setEnabled(true);
                                btnIncrease.setOnClickListener(v -> {
                                    String currentText = tvAmount.getText()
                                            .toString().trim();
                                    if (!currentText.isEmpty()) {
                                        donationAmount = Integer.parseInt(
                                                currentText);
                                    }
                                    if (donationAmount < qty) {
                                        donationAmount++;
                                        tvAmount.setText(String.valueOf(
                                                donationAmount));
                                        updateDonateButton();
                                    } else {
                                        Toast.makeText(
                                                SingleRecipientActivity.this,
                                                "Maximum available is " + qty,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            btnProfile.setOnClickListener(v -> {
                                Intent intent = new Intent(
                                        SingleRecipientActivity.this,
                                        ProfileActivity.class);
                                intent.putExtra("recipient_id", recipientId);
                                startActivity(intent);
                            });

                            btnDonate.setOnClickListener(v ->
                                    recordDonation(requestId, catId));

                            fetchSimilarRequests(requestId, catId, recipientId);

                        } else {
                            Toast.makeText(SingleRecipientActivity.this,
                                    "Failed to load: "
                                            + obj.getString("message"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(SingleRecipientActivity.this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void fetchSimilarRequests(String requestId,
                                      String catId,
                                      String recipientId) {
        RequestBody requestBody = new FormBody.Builder()
                .add("request_id", requestId)
                .add("cat_id", catId)
                .add("recipient_id", recipientId)
                .build();

        Request request = new Request.Builder()
                .url(baseUrl + "get_similar_requests.php")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call,
                                  @NonNull IOException e) {}

            @Override
            public void onResponse(@NonNull Call call,
                                   @NonNull Response response)
                    throws IOException {
                final String body = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(body);
                        if (obj.getBoolean("success")) {
                            JSONArray similar = obj.getJSONArray("similar");
                            List<String> similarList = new ArrayList<>();
                            List<String[]> similarMeta = new ArrayList<>();

                            for (int i = 0; i < similar.length(); i++) {
                                JSONObject s = similar.getJSONObject(i);
                                similarList.add(
                                        s.getString("full_name")
                                                + " — "
                                                + s.getString("item_name")
                                                + " — Needs "
                                                + s.getString("quantity"));
                                similarMeta.add(new String[]{
                                        s.getString("request_id"),
                                        s.getString("recipient_id"),
                                        s.getString("full_name")
                                });
                            }

                            if (similarList.isEmpty()) {
                                similarList.add("No similar opportunities found");
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    SingleRecipientActivity.this,
                                    android.R.layout.simple_list_item_1,
                                    similarList);
                            lvSimilarOpportunities.setAdapter(adapter);

                            lvSimilarOpportunities.setOnItemClickListener(
                                    (parent, view, position, id) -> {
                                        if (position < similarMeta.size()) {
                                            String[] meta = similarMeta.get(position);
                                            Intent intent = new Intent(
                                                    SingleRecipientActivity.this,
                                                    SingleRecipientActivity.class);
                                            intent.putExtra("request_id", meta[0]);
                                            intent.putExtra("recipient_id", meta[1]);
                                            intent.putExtra("recipient_name", meta[2]);
                                            startActivity(intent);
                                        }
                                    });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    private void recordDonation(String requestId, String catId) {
        if (donorId == -1) {
            Toast.makeText(this, "Error: donor not found",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String currentText = tvAmount.getText().toString().trim();
        if (!currentText.isEmpty()) {
            donationAmount = Integer.parseInt(currentText);
        }

        if (availableQty > 0 && donationAmount > availableQty) {
            donationAmount = availableQty;
            tvAmount.setText(String.valueOf(availableQty));
        }

        btnDonate.setEnabled(false);

        RequestBody requestBody = new FormBody.Builder()
                .add("donor_id", String.valueOf(donorId))
                .add("request_id", requestId)
                .add("quantity_donated", String.valueOf(donationAmount))
                .build();

        Request request = new Request.Builder()
                .url(baseUrl + "save_donation_record.php")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call,
                                  @NonNull IOException e) {
                runOnUiThread(() -> {
                    btnDonate.setEnabled(true);
                    Toast.makeText(SingleRecipientActivity.this,
                            "Failed to record donation",
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call,
                                   @NonNull Response response)
                    throws IOException {
                final String body = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(body);
                        if (obj.getBoolean("success")) {
                            Toast.makeText(SingleRecipientActivity.this,
                                    "Thank you! You donated "
                                            + donationAmount + " item(s)!",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            btnDonate.setEnabled(true);
                            Toast.makeText(SingleRecipientActivity.this,
                                    "Failed: " + obj.getString("message"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        btnDonate.setEnabled(true);
                        Toast.makeText(SingleRecipientActivity.this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}