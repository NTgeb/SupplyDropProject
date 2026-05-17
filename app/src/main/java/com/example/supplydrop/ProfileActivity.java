package com.example.supplydrop;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileActivity extends AppCompatActivity {

    TextView tvRecipientName, tvProfileDescription, tvWebsite,
            tvAddress, tvPhone, tvEmail;
    ImageView imgProfileLogo;
    Button btnViewOtherDonations;

    OkHttpClient client = new OkHttpClient();
    String baseUrl = "https://wmc.ms.wits.ac.za/students/sgroup2711/";

    String recipientId;
    String recipientName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvRecipientName      = findViewById(R.id.tvRecipientName);
        tvProfileDescription = findViewById(R.id.tvProfileDescription);
        tvWebsite            = findViewById(R.id.tvWebsite);
        tvAddress            = findViewById(R.id.tvAddress);
        tvPhone              = findViewById(R.id.tvPhone);
        tvEmail              = findViewById(R.id.tvEmail);
        imgProfileLogo       = findViewById(R.id.imgProfileLogo);
        btnViewOtherDonations = findViewById(R.id.btnViewOtherDonations);

        recipientId = getIntent().getStringExtra("recipient_id");

        fetchProfile();
    }

    private void fetchProfile() {
        RequestBody requestBody = new FormBody.Builder()
                .add("recipient_id", recipientId)
                .build();

        Request request = new Request.Builder()
                .url(baseUrl + "get_profile.php")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call,
                                  @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(ProfileActivity.this,
                                "Failed to load profile",
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
                            recipientName      = obj.getString("full_name");
                            String phone       = obj.optString("cellphone", "");
                            String email       = obj.optString("email", "");
                            String address     = obj.optString("address", "");
                            String city        = obj.optString("city", "");
                            String description = obj.optString("description", "");
                            String website     = obj.optString("website", "");
                            String profileImage = obj.optString(
                                    "profile_image", "");

                            tvRecipientName.setText(nullToEmpty(recipientName));

                            tvProfileDescription.setText(
                                    nullToEmpty(description).isEmpty()
                                            ? "No description provided"
                                            : nullToEmpty(description));

                            tvPhone.setText(nullToEmpty(phone).isEmpty()
                                    ? "📞  No phone provided"
                                    : "📞  " + nullToEmpty(phone));

                            tvEmail.setText(nullToEmpty(email).isEmpty()
                                    ? "✉  No email provided"
                                    : "✉  " + nullToEmpty(email));

                            String cleanAddress = nullToEmpty(address);
                            String cleanCity    = nullToEmpty(city);
                            if (cleanAddress.isEmpty() && cleanCity.isEmpty()) {
                                tvAddress.setText("No address provided");
                            } else if (cleanCity.isEmpty()) {
                                tvAddress.setText(cleanAddress);
                            } else {
                                tvAddress.setText(cleanAddress + ", " + cleanCity);
                            }

                            String cleanWebsite = nullToEmpty(website);
                            tvWebsite.setText(cleanWebsite.isEmpty()
                                    ? "No website provided"
                                    : cleanWebsite);

                            // Load profile image using Glide
                            if (!profileImage.isEmpty()
                                    && !profileImage.equals("null")) {
                                Glide.with(ProfileActivity.this)
                                        .load(profileImage)
                                        .placeholder(R.drawable.account_circle)
                                        .into(imgProfileLogo);
                            }

                            btnViewOtherDonations.setOnClickListener(v -> {
                                Intent intent = new Intent(
                                        ProfileActivity.this,
                                        DonorActivity.class);
                                intent.putExtra("filter_recipient",
                                        recipientName);
                                intent.setFlags(
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            });

                        } else {
                            Toast.makeText(ProfileActivity.this,
                                    "Profile not found",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(ProfileActivity.this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private String nullToEmpty(String value) {
        if (value == null || value.equals("null")) return "";
        return value;
    }
}