package com.example.supplydrop;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    TextView tvRecipientName, tvProfileDescription, tvWebsite,
            tvAddress, tvPhone, tvEmail;
    Button btnViewOtherDonations;

    // Placeholder data — replace with DB later
    String recipientName  = "John Doe";
    String recipientPhone = "071 234 5678";
    String recipientEmail = "john@example.com";
    String recipientAddress = "123 Example Street, Johannesburg, 2000";
    String recipientWebsite = "www.example.org";
    String recipientDesc  = "I am a father of 5 struggling to provide " +
            "basic food supplies for my family this month. " +
            "Any help is greatly appreciated.";

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
        btnViewOtherDonations = findViewById(R.id.btnViewOtherDonations);

        // Fill screen with placeholder data
        tvRecipientName.setText(recipientName);
        tvProfileDescription.setText(recipientDesc);
        tvPhone.setText("📞  " + recipientPhone);
        tvEmail.setText("✉  " + recipientEmail);
        tvAddress.setText(recipientAddress);

        // Only show website if it exists
        if (recipientWebsite.isEmpty()) {
            tvWebsite.setText("No website provided");
        } else {
            tvWebsite.setText(recipientWebsite);
        }

        // View Other Donations → back to HomeActivity
        btnViewOtherDonations.setOnClickListener(v -> {
            // Intent intent = new Intent(this, HomeActivity.class);
            // startActivity(intent);
        });
    }
}