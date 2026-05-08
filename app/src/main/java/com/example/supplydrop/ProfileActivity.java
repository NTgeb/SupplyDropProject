package com.example.supplydrop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    // Step 1: Declare all components
    TextView tvLocation, tvProfileDescription, tvProfileCategory,
            tvWebsite, tvAddress;
    Button btnConnect, btnViewOtherDonations;

    // Step 2: Placeholder data — later this will come from the database
    String recipientName     = "John Doe";
    String recipientPhone    = "071 234 5678";
    String recipientAddress  = "123 Example Street, Johannesburg, 2000";
    String recipientCategory = "Food";
    String recipientWebsite  = "www.example.org";
    String recipientLocation = "Johannesburg, Gauteng";
    String recipientDesc     = "I am a father of 5 struggling to provide " +
            "basic food supplies for my family this month. " +
            "Any help is greatly appreciated.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Step 3: Connect variables to XML components
        tvProfileDescription = findViewById(R.id.tvProfileDescription);
        tvWebsite            = findViewById(R.id.tvWebsite);
        tvAddress            = findViewById(R.id.tvAddress);
        btnConnect           = findViewById(R.id.btnConnect);
        btnViewOtherDonations = findViewById(R.id.btnViewOtherDonations);

        // Step 4: Fill the screen with placeholder data
        tvLocation.setText("📍 " + recipientLocation);
        tvProfileDescription.setText(recipientDesc);
        tvProfileCategory.setText(recipientCategory);
        tvAddress.setText(recipientAddress);

        // Step 5: Only show website if it exists, hide it if empty
        if (recipientWebsite.isEmpty()) {
            tvWebsite.setText("No website provided");
            tvWebsite.setTextColor(getResources().getColor(android.R.color.darker_gray));
        } else {
            tvWebsite.setText(recipientWebsite);
        }

        // Step 6: Connect button shows the phone number
        // Later this will open the phone dialer
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this,
                        "📞 Contact: " + recipientPhone,
                        Toast.LENGTH_LONG).show();
            }
        });



        // Step 8: View Other Donations goes to the Donor Dashboard
        // We will create DonorDashboardActivity later
        btnViewOtherDonations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            /*    Intent intent = new Intent(ProfileActivity.this,
                        DonorDashboardActivity.class);
                startActivity(intent); */
            }
        });
    }
}