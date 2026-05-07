package com.example.supplydrop;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class SingleRecipientActivity extends AppCompatActivity {

    // Step 1: Declare all the components we need to use
    TextView tvRecipientTitle, tvDescription, tvCategory, tvAmount;
    Button btnProfile, btnIncrease, btnDecrease, btnDonate;
    ListView lvSimilarOpportunities;

    // Step 2: This keeps track of how many items the donor wants to donate
    int donationAmount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_recipient);

        // Step 3: Connect each variable to its component in the XML
        // The id names here must match the ids in your XML file
        tvRecipientTitle = findViewById(R.id.tvRecipientTitle);
        tvDescription = findViewById(R.id.tvDescription);
        tvCategory = findViewById(R.id.tvCategory);
        tvAmount = findViewById(R.id.tvAmount);
        btnProfile = findViewById(R.id.btnProfile);
        btnIncrease = findViewById(R.id.btnIncrease);
        btnDecrease = findViewById(R.id.btnDecrease);
        btnDonate = findViewById(R.id.btnDonate);
        lvSimilarOpportunities = findViewById(R.id.lvSimilarOpportunities);

        // Step 4: Set placeholder data for now
        // Later this will come from your database via PHP
        tvRecipientTitle.setText("John Doe");
        tvDescription.setText("I am in need of food supplies for my family of 5. " +
                "We are struggling to get basic necessities this month.");
        tvCategory.setText("Category: Food");
        tvAmount.setText(String.valueOf(donationAmount));

        // Step 5: Set up the Similar Opportunities list with placeholder data
        // Later this will also come from your database
        ArrayList<String> similarList = new ArrayList<>();
        similarList.add("Jane Smith — Food — Needs 3 items");
        similarList.add("Hope NGO — Hygiene — Needs 10 items");
        similarList.add("Mary Johnson — Food — Needs 5 items");
        similarList.add("Help Centre — Construction — Needs 2 items");

        // ArrayAdapter connects the list of text to the ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                similarList
        );
        lvSimilarOpportunities.setAdapter(adapter);

        // Step 6: Increase button — adds 1 to the amount
        btnIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                donationAmount++;
                tvAmount.setText(String.valueOf(donationAmount));
            }
        });

        // Step 7: Decrease button — removes 1 from amount
        // But we stop it going below 1 so donor cant donate 0 or negative
        btnDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (donationAmount > 1) {
                    donationAmount--;
                    tvAmount.setText(String.valueOf(donationAmount));
                } else {
                    Toast.makeText(SingleRecipientActivity.this,
                            "Minimum donation is 1", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Step 8: Donate button — for now shows a confirmation message
        // Later this will send data to your database
        btnDonate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SingleRecipientActivity.this,
                        "You donated " + donationAmount + " item(s)!",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Step 9: Profile button — goes to the Profile screen
        // We will create ProfileActivity later
       /* btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This line opens the ProfileActivity screen
                // We will create this screen in the next step
                // Intent is like telling the app "go to this screen"
                Intent intent = new Intent(SingleRecipientActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        })
        ;
        */
    }
}