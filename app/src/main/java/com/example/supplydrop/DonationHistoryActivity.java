package com.example.supplydrop;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class DonationHistoryActivity extends AppCompatActivity {

    ListView donationHistoryListView;
    DonationHistoryAdapter adapter;

    // Dummy data - replace with DB later
    // Each entry: { organisationName, item, amountDonated }
    List<String[]> allDonations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_history);

        donationHistoryListView = findViewById(R.id.donationHistoryListView);

        setupDummyData();
        setupList();
    }

    private void setupDummyData() {
        allDonations.add(new String[]{"Hope Foundation", "T-Shirts", "10"});
        allDonations.add(new String[]{"Ubuntu Centre", "Soap", "5"});
        allDonations.add(new String[]{"Helping Hands", "Rice", "20"});
    }

    private void setupList() {
        adapter = new DonationHistoryAdapter(this, allDonations);
        donationHistoryListView.setAdapter(adapter);

        // Click row → go to that recipient/organisation's profile page
        donationHistoryListView.setOnItemClickListener((parent, view, position, id) -> {
            String[] donation = allDonations.get(position);
            Intent intent = new Intent(this, SingleRecipientActivity.class);
            intent.putExtra("recipientName", donation[0]);
            startActivity(intent);
        });
    }
}