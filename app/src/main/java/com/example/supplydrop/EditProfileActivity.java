package com.example.supplydrop;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {

    EditText recipientNameEt, descriptionEt, phoneEt, emailEt, websiteEt, addressEt;
    ImageView profileImageView;
    Button changeImageBtn, saveProfileBtn;
    Uri selectedImageUri = null;

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

        recipientNameEt = findViewById(R.id.recipientNameEt);
        descriptionEt = findViewById(R.id.descriptionEt);
        phoneEt = findViewById(R.id.phoneEt);
        emailEt = findViewById(R.id.emailEt);
        websiteEt = findViewById(R.id.websiteEt);
        addressEt = findViewById(R.id.addressEt);
        profileImageView = findViewById(R.id.profileImageView);
        changeImageBtn = findViewById(R.id.changeImageBtn);
        saveProfileBtn = findViewById(R.id.saveProfileBtn);

        loadExistingProfile();
        setupImagePicker();
        setupSaveButton();
    }

    private void loadExistingProfile() {
        // TODO: Load existing profile data from DB when ready
        // For now fields start empty
    }

    private void setupImagePicker() {
        changeImageBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });
    }

    private void setupSaveButton() {
        saveProfileBtn.setOnClickListener(v -> {
            String name = recipientNameEt.getText().toString().trim();
            String description = descriptionEt.getText().toString().trim();
            String phone = phoneEt.getText().toString().trim();
            String email = emailEt.getText().toString().trim();
            String website = websiteEt.getText().toString().trim();
            String address = addressEt.getText().toString().trim();

            // Basic validation
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

            // TODO: Save to database when ready
            Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show();
            finish(); // goes back to account screen
        });
    }
}