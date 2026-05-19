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

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditProfileActivity extends AppCompatActivity {

    EditText recipientNameEt, descriptionEt, phoneEt, cityEt, websiteEt, addressEt;
    ImageView profileImageView;
    Button changeImageBtn, saveProfileBtn;
    Uri selectedImageUri = null;
    String uploadedImageUrl = null;

    OkHttpClient client = new OkHttpClient();
    String baseUrl = "https://wmc.ms.wits.ac.za/students/sgroup2711/";
    int recipientId;

    ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            selectedImageUri = result.getData().getData();
                            profileImageView.setImageURI(selectedImageUri);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //Variables to objects
        recipientNameEt  = findViewById(R.id.recipientNameEt);
        descriptionEt    = findViewById(R.id.descriptionEt);
        phoneEt          = findViewById(R.id.phoneEt);
        cityEt          = findViewById(R.id.cityEt);
        websiteEt        = findViewById(R.id.websiteEt);
        addressEt        = findViewById(R.id.addressEt);
        profileImageView = findViewById(R.id.profileImageView);
        changeImageBtn   = findViewById(R.id.changeImageBtn);
        saveProfileBtn   = findViewById(R.id.saveProfileBtn);

        //Getting the info of the user from the local storage
        SharedPreferences prefs = getSharedPreferences("SupplyDropPrefs", MODE_PRIVATE);
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
                            recipientNameEt.setText(
                                    nullToEmpty(obj.optString("full_name")));
                            descriptionEt.setText(
                                    nullToEmpty(obj.optString("description")));
                            phoneEt.setText(
                                    nullToEmpty(obj.optString("cellphone")));
                            cityEt.setText(
                                    nullToEmpty(obj.optString("city")));
                            websiteEt.setText(
                                    nullToEmpty(obj.optString("website")));
                            addressEt.setText(
                                    nullToEmpty(obj.optString("address")));

                            uploadedImageUrl = nullToEmpty(
                                    obj.optString("profile_image"));

                            if (!uploadedImageUrl.isEmpty()) {
                                Glide.with(EditProfileActivity.this)
                                        .load(uploadedImageUrl)
                                        .placeholder(R.drawable.account_circle)
                                        .into(profileImageView);
                            }
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

    private void setupSaveButton() {
        saveProfileBtn.setOnClickListener(v -> {
            String name = recipientNameEt.getText().toString().trim();
            String description = descriptionEt.getText().toString().trim();
            String phone = phoneEt.getText().toString().trim();
            String city = cityEt.getText().toString().trim();
            String website = websiteEt.getText().toString().trim();
            String address = addressEt.getText().toString().trim();

            if (name.isEmpty()) {
                recipientNameEt.setError("Please enter a name");
                return;
            }
            if (phone.isEmpty()) {
                phoneEt.setError("Please enter a phone number");
                return;
            }
            if (city.isEmpty()) {
                cityEt.setError("Please enter an city");
                return;
            }

            saveProfileBtn.setEnabled(false);
            Toast.makeText(this, "Saving...", Toast.LENGTH_SHORT).show();

            if (selectedImageUri != null) {
                uploadImageThenSave(name, description, phone,
                        city, website, address);
            } else {
                saveToDatabase(name, description, phone,
                        city, website, address, uploadedImageUrl);
            }
        });
    }

    private void uploadImageThenSave(String name, String description,
                                     String phone, String city,
                                     String website, String address) {
        try {
            java.io.InputStream inputStream = getContentResolver()
                    .openInputStream(selectedImageUri);
            byte[] imageBytes = new byte[inputStream.available()];
            inputStream.read(imageBytes);
            inputStream.close();

            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image",
                            "profile_" + recipientId + ".jpg",
                            RequestBody.create(
                                    imageBytes,
                                    MediaType.parse("image/jpeg")))
                    .build();

            Request request = new Request.Builder()
                    .url(baseUrl + "upload_image.php")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call,
                                      @NonNull IOException e) {
                    runOnUiThread(() -> {
                        saveProfileBtn.setEnabled(true);
                        Toast.makeText(EditProfileActivity.this,
                                "Image upload failed",
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
                                String imageUrl = obj.getString("image_url");
                                saveToDatabase(name, description, phone,
                                        city, website, address, imageUrl);
                            } else {
                                saveProfileBtn.setEnabled(true);
                                Toast.makeText(EditProfileActivity.this,
                                        "Upload failed: "
                                                + obj.getString("message"),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            saveProfileBtn.setEnabled(true);
                            Toast.makeText(EditProfileActivity.this,
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } catch (Exception e) {
            saveProfileBtn.setEnabled(true);
            Toast.makeText(this,
                    "Error reading image: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void saveToDatabase(String name, String description,
                                String phone, String city,
                                String website, String address,
                                String imageUrl) {
        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("recipient_id", String.valueOf(recipientId))
                .add("full_name",    name)
                .add("description",  description)
                .add("cellphone",    phone)
                .add("city",        city)
                .add("website",      website)
                .add("address",      address);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            formBuilder.add("profile_image", imageUrl);
        }

        RequestBody requestBody = formBuilder.build();
        Request request = new Request.Builder()
                .url(baseUrl + "update_profile.php")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call,
                                  @NonNull IOException e) {
                runOnUiThread(() -> {
                    saveProfileBtn.setEnabled(true);
                    Toast.makeText(EditProfileActivity.this,
                            "Connection failed",
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
                            Toast.makeText(EditProfileActivity.this,
                                    "Profile updated!",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            saveProfileBtn.setEnabled(true);
                            Toast.makeText(EditProfileActivity.this,
                                    obj.getString("message"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        saveProfileBtn.setEnabled(true);
                        Toast.makeText(EditProfileActivity.this,
                                "Error: " + body,
                                Toast.LENGTH_LONG).show();
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