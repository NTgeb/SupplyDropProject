package com.example.supplydrop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditProfileActivity extends AppCompatActivity {

    EditText recipientNameEt, descriptionEt, phoneEt,
            emailEt, websiteEt, addressEt;
    ImageView profileImageView;
    Button changeImageBtn, saveProfileBtn;
    Uri selectedImageUri = null;

    OkHttpClient client = new OkHttpClient();
    String baseUrl = "https://wmc.ms.wits.ac.za/students/sgroup2711/";
    int recipientId;

    ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK
                                && result.getData() != null) {
                            selectedImageUri = result.getData().getData();
                            profileImageView.setImageURI(selectedImageUri);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        recipientNameEt  = findViewById(R.id.recipientNameEt);
        descriptionEt    = findViewById(R.id.descriptionEt);
        phoneEt          = findViewById(R.id.phoneEt);
        emailEt          = findViewById(R.id.emailEt);
        websiteEt        = findViewById(R.id.websiteEt);
        addressEt        = findViewById(R.id.addressEt);
        profileImageView = findViewById(R.id.profileImageView);
        changeImageBtn   = findViewById(R.id.changeImageBtn);
        saveProfileBtn   = findViewById(R.id.saveProfileBtn);

        // Get recipient_id from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(
                "SupplyDropPrefs", MODE_PRIVATE);
        recipientId = prefs.getInt("recipient_id", -1);

        loadExistingProfile();
        setupImagePicker();
        setupSaveButton();
    }

    private void loadExistingProfile() {
        RequestBody requestBody = new FormBody.Builder()
                .add("recipient_id", String.valueOf(recipientId))
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
                        Toast.makeText(EditProfileActivity.this,
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
                            // Pre-fill all fields with existing data
                            recipientNameEt.setText(nullToEmpty(obj.optString("full_name", "")));
                            descriptionEt.setText(nullToEmpty(obj.optString("description", "")));
                            phoneEt.setText(nullToEmpty(obj.optString("cellphone", "")));
                            emailEt.setText(nullToEmpty(obj.optString("email", "")));
                            websiteEt.setText(nullToEmpty(obj.optString("website", "")));
                            addressEt.setText(nullToEmpty(obj.optString("address", "")));
                        }
                    } catch (Exception e) {
                        Toast.makeText(EditProfileActivity.this,
                                "Error loading profile",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void setupImagePicker() {
        changeImageBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media
                            .EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });
    }

    private void setupSaveButton()
    {
        saveProfileBtn.setOnClickListener(v -> {
            String name        = recipientNameEt.getText()
                    .toString().trim();
            String description = descriptionEt.getText()
                    .toString().trim();
            String phone       = phoneEt.getText().toString().trim();
            String email       = emailEt.getText().toString().trim();
            String website     = websiteEt.getText().toString().trim();
            String address     = addressEt.getText().toString().trim();

            if (name.isEmpty()) {
                recipientNameEt.setError("Please enter a name");
                return;
            }
            if (phone.isEmpty()) {
                phoneEt.setError("Please enter a phone number");
                return;
            }
            if (email.isEmpty()) {
                emailEt.setError("Please enter an email");
                return;
            }

            RequestBody requestBody = new FormBody.Builder()
                    .add("recipient_id", String.valueOf(recipientId))
                    .add("full_name",    name)
                    .add("description",  description)
                    .add("cellphone",    phone)
                    .add("email",        email)
                    .add("website",      website)
                    .add("address",      address)
                    .build();

            Request request = new Request.Builder()
                    .url(baseUrl + "update_profile.php")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call,
                                      @NonNull IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(EditProfileActivity.this,
                                    "Connection failed",
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
                                Toast.makeText(EditProfileActivity.this,
                                        "Profile updated!",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(EditProfileActivity.this,
                                        obj.getString("message"),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(EditProfileActivity.this,
                                    "Error: " + body,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        });
    }
    private String nullToEmpty(String value) {
        if (value == null || value.equals("null")) return "";
        return value;
    }
}