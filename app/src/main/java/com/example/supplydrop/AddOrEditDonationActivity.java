package com.example.supplydrop;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddOrEditDonationActivity extends AppCompatActivity {

    EditText itemNameEt, descriptionEt;
    Spinner categorySpinner;
    TextView quantityTv;
    Button decreaseQtyBtn, increaseQtyBtn, changeImageBtn, saveBtn;
    ImageView itemImageView;

    int quantity = 1;
    Uri selectedImageUri = null;
    String uploadedImageUrl = null;
    String mode = "add";

    OkHttpClient client = new OkHttpClient();
    List<String> categoryNames = new ArrayList<>();
    List<Integer> categoryIds = new ArrayList<>();
    int selectedCatId = -1;
    int recipientId = -1;
    int requestId = -1;

    String getCategoriesUrl =
            "https://wmc.ms.wits.ac.za/students/sgroup2711/get_categories.php";
    String saveDonationUrl =
            "https://wmc.ms.wits.ac.za/students/sgroup2711/save_donation.php";
    String uploadImageUrl =
            "https://wmc.ms.wits.ac.za/students/sgroup2711/upload_image.php";

    ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK
                                && result.getData() != null) {
                            selectedImageUri = result.getData().getData();
                            itemImageView.setImageURI(selectedImageUri);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_or_edit_donation);

        itemNameEt     = findViewById(R.id.itemNameEt);
        descriptionEt  = findViewById(R.id.descriptionEt);
        categorySpinner = findViewById(R.id.categorySpinner);
        quantityTv     = findViewById(R.id.quantityTv);
        decreaseQtyBtn = findViewById(R.id.decreaseQtyBtn);
        increaseQtyBtn = findViewById(R.id.increaseQtyBtn);
        changeImageBtn = findViewById(R.id.changeImageBtn);
        saveBtn        = findViewById(R.id.saveBtn);
        itemImageView  = findViewById(R.id.itemImageView);

        recipientId = getIntent().getIntExtra("recipient_id", -1);
        requestId   = getIntent().getIntExtra("request_id", -1);

        setupCategorySpinner();
        setupQuantityButtons();
        setupImagePicker();
        checkMode();
        setupSaveButton();
    }

    private void setupCategorySpinner() {
        RequestBody requestBody = new FormBody.Builder().build();
        Request request = new Request.Builder()
                .url(getCategoriesUrl)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call,
                                  @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(AddOrEditDonationActivity.this,
                                "Failed to load categories",
                                Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call,
                                   @NonNull Response response)
                    throws IOException {
                String responseBody = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(responseBody);
                        if (obj.getBoolean("success")) {
                            JSONArray categories =
                                    obj.getJSONArray("categories");
                            for (int i = 0; i < categories.length(); i++) {
                                JSONObject cat = categories.getJSONObject(i);
                                categoryNames.add(cat.getString("cat_name"));
                                categoryIds.add(cat.getInt("cat_id"));
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    AddOrEditDonationActivity.this,
                                    android.R.layout.simple_spinner_item,
                                    categoryNames);
                            adapter.setDropDownViewResource(
                                    android.R.layout.simple_spinner_dropdown_item);
                            categorySpinner.setAdapter(adapter);

                            if (mode.equals("edit")) {
                                int catId = getIntent().getIntExtra(
                                        "cat_id", -1);
                                int index = categoryIds.indexOf(catId);
                                if (index >= 0)
                                    categorySpinner.setSelection(index);
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(AddOrEditDonationActivity.this,
                                "Error: " + responseBody,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void setupQuantityButtons() {
        decreaseQtyBtn.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                quantityTv.setText(String.valueOf(quantity));
            }
        });

        increaseQtyBtn.setOnClickListener(v -> {
            quantity++;
            quantityTv.setText(String.valueOf(quantity));
        });
    }

    private void setupImagePicker() {
        changeImageBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });
    }

    private void checkMode() {
        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "add";

        if (mode.equals("edit")) {
            String existingName = getIntent().getStringExtra("itemName");
            String existingDesc = getIntent().getStringExtra("description");
            String existingQty  = getIntent().getStringExtra("quantity");
            String existingImage = getIntent().getStringExtra("item_image");
            requestId = getIntent().getIntExtra("request_id", -1);

            if (existingName != null) itemNameEt.setText(existingName);
            if (existingDesc != null) descriptionEt.setText(existingDesc);
            if (existingQty != null) {
                quantity = Integer.parseInt(existingQty);
                quantityTv.setText(existingQty);
            }

            // Load existing item image if available
            if (existingImage != null && !existingImage.isEmpty()
                    && !existingImage.equals("null")) {
                uploadedImageUrl = existingImage;
                Glide.with(this)
                        .load(existingImage)
                        .placeholder(R.drawable.account_circle)
                        .into(itemImageView);
            }
        }
    }

    private void setupSaveButton() {
        saveBtn.setOnClickListener(v -> {
            String itemName    = itemNameEt.getText().toString().trim();
            String description = descriptionEt.getText().toString().trim();

            if (itemName.isEmpty()) {
                itemNameEt.setError("Please enter an item name");
                return;
            }
            if (description.isEmpty()) {
                descriptionEt.setError("Please enter a description");
                return;
            }
            if (categorySpinner.getSelectedItem() == null) {
                Toast.makeText(this, "Please select a category",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (recipientId == -1) {
                Toast.makeText(this, "Error: recipient not found",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            int spinnerIndex = categorySpinner.getSelectedItemPosition();
            selectedCatId = categoryIds.get(spinnerIndex);

            FormBody.Builder formBuilder = new FormBody.Builder()
                    .add("recipient_id", String.valueOf(recipientId))
                    .add("item_name",    itemName)
                    .add("description",  description)
                    .add("quantity",     String.valueOf(quantity))
                    .add("cat_id",       String.valueOf(selectedCatId))
                    .add("mode",         mode);

            if (mode.equals("edit")) {
                formBuilder.add("request_id", String.valueOf(requestId));
            }

            if (uploadedImageUrl != null && !uploadedImageUrl.isEmpty()) {
                formBuilder.add("item_image", uploadedImageUrl);
            }

            saveBtn.setEnabled(false);

            if (selectedImageUri != null) {
                uploadImageThenSave(formBuilder);
            } else {
                sendSaveRequest(formBuilder);
            }
        });
    }

    private void uploadImageThenSave(FormBody.Builder formBuilder) {
        try {
            java.io.InputStream inputStream = getContentResolver()
                    .openInputStream(selectedImageUri);
            byte[] imageBytes = new byte[inputStream.available()];
            inputStream.read(imageBytes);
            inputStream.close();

            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image",
                            "item_" + System.currentTimeMillis() + ".jpg",
                            RequestBody.create(
                                    imageBytes,
                                    MediaType.parse("image/jpeg")))
                    .build();

            Request request = new Request.Builder()
                    .url(uploadImageUrl)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call,
                                      @NonNull IOException e) {
                    runOnUiThread(() -> {
                        saveBtn.setEnabled(true);
                        Toast.makeText(AddOrEditDonationActivity.this,
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
                                formBuilder.add("item_image", imageUrl);
                                sendSaveRequest(formBuilder);
                            } else {
                                saveBtn.setEnabled(true);
                                Toast.makeText(AddOrEditDonationActivity.this,
                                        "Upload failed: "
                                                + obj.getString("message"),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            saveBtn.setEnabled(true);
                        }
                    });
                }
            });
        } catch (Exception e) {
            saveBtn.setEnabled(true);
            Toast.makeText(this,
                    "Error reading image: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSaveRequest(FormBody.Builder formBuilder) {
        RequestBody requestBody = formBuilder.build();
        Request request = new Request.Builder()
                .url(saveDonationUrl)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call,
                                  @NonNull IOException e) {
                runOnUiThread(() -> {
                    saveBtn.setEnabled(true);
                    Toast.makeText(AddOrEditDonationActivity.this,
                            "Connection failed",
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call,
                                   @NonNull Response response)
                    throws IOException {
                String responseBody = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(responseBody);
                        if (obj.getBoolean("success")) {
                            Toast.makeText(AddOrEditDonationActivity.this,
                                    mode.equals("add") ?
                                            "Donation added!" :
                                            "Donation updated!",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            saveBtn.setEnabled(true);
                            Toast.makeText(AddOrEditDonationActivity.this,
                                    obj.getString("message"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        saveBtn.setEnabled(true);
                        Toast.makeText(AddOrEditDonationActivity.this,
                                "Error: " + responseBody,
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}