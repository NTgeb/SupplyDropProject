package com.example.supplydrop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

    TextView tvRecipientTitle, tvDescription, tvCategory,
            tvAmount, tvQuantityNeeded;
    Button btnProfile, btnIncrease, btnDecrease, btnDonate;
    ListView lvSimilarOpportunities;

    int donationAmount = 1;
    int donorId;
    String requestId, recipientId, catName;

    OkHttpClient client = new OkHttpClient();
    String baseUrl = "https://wmc.ms.wits.ac.za/students/sgroup2711/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_recipient);

        tvRecipientTitle   = findViewById(R.id.tvRecipientTitle);
        tvDescription      = findViewById(R.id.tvDescription);
        tvCategory         = findViewById(R.id.tvCategory);
        tvAmount           = findViewById(R.id.tvAmount);
        tvQuantityNeeded   = findViewById(R.id.tvQuantityNeeded);
        btnProfile         = findViewById(R.id.btnProfile);
        btnIncrease        = findViewById(R.id.btnIncrease);
        btnDecrease        = findViewById(R.id.btnDecrease);
        btnDonate          = findViewById(R.id.btnDonate);
        lvSimilarOpportunities = findViewById(R.id.lvSimilarOpportunities);

        // Get donor_id from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(
                "SupplyDropPrefs", MODE_PRIVATE);
        donorId = prefs.getInt("donor_id", -1);

        // Get request_id and recipient_id passed from DonorDashboardFragment
        requestId   = getIntent().getStringExtra("request_id");
        recipientId = getIntent().getStringExtra("recipient_id");

        // Amount controls
        tvAmount.setText(String.valueOf(donationAmount));

        btnIncrease.setOnClickListener(v -> {
            donationAmount++;
            tvAmount.setText(String.valueOf(donationAmount));
        });

        btnDecrease.setOnClickListener(v -> {
            if (donationAmount > 1) {
                donationAmount--;
                tvAmount.setText(String.valueOf(donationAmount));
            } else {
                Toast.makeText(this,
                        "Minimum donation is 1",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Fetch request details from DB
        fetchRequestDetails();
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
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(SingleRecipientActivity.this,
                                "Failed to load request",
                                Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call,
                                   @NonNull Response response) throws IOException {
                final String body = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(body);
                        if (obj.getBoolean("success")) {
                            String itemName    = obj.getString("item_name");
                            String description = obj.optString(
                                    "description", "No description yet");
                            String quantity    = obj.getString("quantity");
                            String fullName    = obj.getString("full_name");
                            catName            = obj.getString("cat_name");
                            String catId       = obj.getString("cat_id") ;

                            // Populate the screen
                            tvRecipientTitle.setText(itemName
                                    + " — " + fullName);
                            tvDescription.setText(
                                    description.isEmpty()
                                            ? "No description yet"
                                            : description);
                            tvCategory.setText("Category: " + catName);
                            tvQuantityNeeded.setText(
                                    quantity + " remaining");

                            int qty = Integer.parseInt(quantity);

                            // Cap donation amount at available quantity
                            if (qty <= 0) {
                                // Disable donate button if nothing left
                                btnDonate.setEnabled(false);
                                btnDonate.setBackgroundTintList(
                                        android.content.res.ColorStateList.valueOf(
                                                android.graphics.Color.GRAY));
                                btnIncrease.setEnabled(false);
                            } else {
                                btnDonate.setEnabled(true);
                                btnDonate.setBackgroundTintList(
                                        android.content.res.ColorStateList.valueOf(
                                                getResources().getColor(R.color.yellow)));
                                // Cap increase button at available quantity
                                btnIncrease.setOnClickListener(v -> {
                                    if (donationAmount < qty) {
                                        donationAmount++;
                                        tvAmount.setText(String.valueOf(donationAmount));
                                    } else {
                                        Toast.makeText(SingleRecipientActivity.this,
                                                "Maximum available is " + qty,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            // Wire up profile button now we have the data
                            btnProfile.setOnClickListener(v -> {
                                Intent intent = new Intent(
                                        SingleRecipientActivity.this,
                                        ProfileActivity.class);
                                intent.putExtra("recipient_id", recipientId);
                                startActivity(intent);
                            });

                            // Wire up donate button
                            btnDonate.setOnClickListener(v ->
                                    recordDonation(requestId, catId));

                            // Fetch similar opportunities
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
                                  @NonNull IOException e) {
                // Silently fail for similar opportunities
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
                            JSONArray similar = obj.getJSONArray("similar");
                            List<String> similarList = new ArrayList<>();

                            for (int i = 0; i < similar.length(); i++) {
                                JSONObject s = similar.getJSONObject(i);
                                similarList.add(
                                        s.getString("full_name")
                                                + " — "
                                                + s.getString("item_name")
                                                + " — Needs "
                                                + s.getString("quantity"));
                            }

                            if (similarList.isEmpty()) {
                                similarList.add(
                                        "No similar opportunities found");
                            }

                            ArrayAdapter<String> adapter =
                                    new ArrayAdapter<>(
                                            SingleRecipientActivity.this,
                                            android.R.layout.simple_list_item_1,
                                            similarList);
                            lvSimilarOpportunities.setAdapter(adapter);
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
            Toast.makeText(this,
                    "Error: donor not found",
                    Toast.LENGTH_SHORT).show();
            return;
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
                            finish(); // go back to donor dashboard
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