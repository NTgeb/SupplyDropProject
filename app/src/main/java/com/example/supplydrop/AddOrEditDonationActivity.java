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

public class AddOrEditDonationActivity extends AppCompatActivity {

    EditText itemNameEt, descriptionEt;
    Spinner categorySpinner;
    TextView quantityTv;
    Button decreaseQtyBtn, increaseQtyBtn, changeImageBtn, saveBtn;
    ImageView itemImageView;

    int quantity = 1;
    Uri selectedImageUri = null;
    String mode = "add"; // "add" or "edit"

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

        setupCategorySpinner();
        setupQuantityButtons();
        setupImagePicker();
        checkMode();
        setupSaveButton();
    }

    private void setupCategorySpinner() {
        // Replace with DB categories later
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Clothing", "Food", "Toiletries"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
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
        // Check if we're editing an existing entry
        // When wiring to DB later, pass the existing data via Intent extras
        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "add";

        if (mode.equals("edit")) {
            // Pre-fill fields with existing data passed from RecipientDashboard
            String existingName = getIntent().getStringExtra("itemName");
            String existingDesc = getIntent().getStringExtra("description");
            String existingQty = getIntent().getStringExtra("quantity");

            if (existingName != null) itemNameEt.setText(existingName);
            if (existingDesc != null) descriptionEt.setText(existingDesc);
            if (existingQty != null) {
                quantity = Integer.parseInt(existingQty);
                quantityTv.setText(existingQty);
            }
        }
    }

    private void setupSaveButton() {
        saveBtn.setOnClickListener(v -> {
            String itemName = itemNameEt.getText().toString().trim();
            String description = descriptionEt.getText().toString().trim();
            String category = categorySpinner.getSelectedItem().toString();

            // Basic validation
            if (itemName.isEmpty()) {
                itemNameEt.setError("Please enter an item name");
                return;
            }
            if (description.isEmpty()) {
                descriptionEt.setError("Please enter a description");
                return;
            }

            // TODO: Save to database when ready
            // For now just show success and go back
            Toast.makeText(this, mode.equals("add") ?
                    "Donation added!" : "Donation updated!", Toast.LENGTH_SHORT).show();
            finish(); // goes back to RecipientDashboard
        });
    }
}