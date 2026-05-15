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
import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;
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

public class AddOrEditDonationActivity extends AppCompatActivity {

    EditText itemNameEt, descriptionEt;
    Spinner categorySpinner;
    TextView quantityTv;
    Button decreaseQtyBtn, increaseQtyBtn, changeImageBtn, saveBtn;
    ImageView itemImageView;

    int quantity = 1;
    Uri selectedImageUri = null;
    String mode = "add"; // "add" or "edit"

    OkHttpClient client = new OkHttpClient();
    List<String> categoryNames = new ArrayList<>();
    List<Integer> categoryIds = new ArrayList<>();
    int selectedCatId = -1;
    int recipientId = -1;
    int requestId = -1;

    String getCategoriesUrl = "https://wmc.ms.wits.ac.za/students/sgroup2711/get_categories.php";
    String saveDonationUrl  = "https://wmc.ms.wits.ac.za/students/sgroup2711/save_donation.php";

    // Image picker launcher
    ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    itemImageView.setImageURI(selectedImageUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_or_edit_donation);

        itemNameEt = findViewById(R.id.itemNameEt);
        descriptionEt = findViewById(R.id.descriptionEt);
        categorySpinner = findViewById(R.id.categorySpinner);
        quantityTv = findViewById(R.id.quantityTv);
        decreaseQtyBtn = findViewById(R.id.decreaseQtyBtn);
        increaseQtyBtn = findViewById(R.id.increaseQtyBtn);
        changeImageBtn = findViewById(R.id.changeImageBtn);
        saveBtn = findViewById(R.id.saveBtn);
        itemImageView = findViewById(R.id.itemImageView);

        // Get the recipient id passed from the login screen
        recipientId = getIntent().getIntExtra("recipient_id", -1);
        requestId   = getIntent().getIntExtra("request_id", -1);
        setupCategorySpinner();
        setupQuantityButtons();
        setupImagePicker();
        checkMode();
        setupSaveButton();
    }

    private void setupCategorySpinner() {
        // Build the request to get categories from the database
        RequestBody requestBody = new FormBody.Builder().build();
        Request request = new Request.Builder()
                .url(getCategoriesUrl)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(AddOrEditDonationActivity.this,
                                "Failed to load categories", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                String responseBody = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(responseBody);
                        if (obj.getBoolean("success")) {
                            JSONArray categories = obj.getJSONArray("categories");

                            // Loop through each category and store name and id
                            for (int i = 0; i < categories.length(); i++) {
                                JSONObject cat = categories.getJSONObject(i);
                                categoryNames.add(cat.getString("cat_name"));
                                categoryIds.add(cat.getInt("cat_id"));
                            }

                            // Set up the spinner with category names
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    AddOrEditDonationActivity.this,
                                    android.R.layout.simple_spinner_item,
                                    categoryNames);
                            adapter.setDropDownViewResource(
                                    android.R.layout.simple_spinner_dropdown_item);
                            categorySpinner.setAdapter(adapter);

                            // If editing, set the spinner to the correct category
                            if (mode.equals("edit")) {
                                int catId = getIntent().getIntExtra("cat_id", -1);
                                int index = categoryIds.indexOf(catId);
                                if (index >= 0) categorySpinner.setSelection(index);
                            }

                        } else {
                            Toast.makeText(AddOrEditDonationActivity.this,
                                    "Could not load categories",
                                    Toast.LENGTH_SHORT).show();
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
            requestId = getIntent().getIntExtra("request_id", -1);

            if (existingName != null) itemNameEt.setText(existingName);
            if (existingDesc != null) descriptionEt.setText(existingDesc);
            if (existingQty != null) {
                quantity = Integer.parseInt(existingQty);
                quantityTv.setText(existingQty);
            }
            // Spinner selection is handled inside setupCategorySpinner
            // once the categories have loaded from the DB
        }
    }


    private void setupSaveButton() {
        saveBtn.setOnClickListener(v -> {
            String itemName   = itemNameEt.getText().toString().trim();
            String description = descriptionEt.getText().toString().trim();

            // Basic validation
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

            // Get the selected category id
            int spinnerIndex = categorySpinner.getSelectedItemPosition();
            selectedCatId = categoryIds.get(spinnerIndex);

            // Build the data to send to PHP
            FormBody.Builder formBuilder = new FormBody.Builder()
                    .add("recipient_id", String.valueOf(recipientId))
                    .add("item_name",    itemName)
                    .add("description",  description)
                    .add("quantity",     String.valueOf(quantity))
                    .add("cat_id",       String.valueOf(selectedCatId))
                    .add("mode",         mode);

            // If editing, also send the request_id
            if (mode.equals("edit")) {
                formBuilder.add("request_id", String.valueOf(requestId));
            }

            RequestBody requestBody = formBuilder.build();
            Request request = new Request.Builder()
                    .url(saveDonationUrl)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(AddOrEditDonationActivity.this,
                                    "Connection failed", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response)
                        throws IOException {
                    String responseBody = response.body().string();
                    runOnUiThread(() -> {
                        try {
                            JSONObject obj = new JSONObject(responseBody);
                            if (obj.getBoolean("success")) {
                                Toast.makeText(AddOrEditDonationActivity.this,
                                        mode.equals("add") ?
                                                "Donation added!" : "Donation updated!",
                                        Toast.LENGTH_SHORT).show();
                                finish(); // go back to dashboard
                            } else {
                                Toast.makeText(AddOrEditDonationActivity.this,
                                        obj.getString("message"),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(AddOrEditDonationActivity.this,
                                    "Error: " + responseBody,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        });
    }
}