package com.example.supplydrop;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class RecipientDashboardActivity extends AppCompatActivity {

    Button addBtn, editBtn, removeBtn;
    ListView donationRequestsListView;

    // Dummy data - replace with DB later
    // Each entry: { itemName, category, quantity }
    List<String[]> allRequests = new ArrayList<>();
    DonationRequestAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipient_dashboard);

        addBtn = findViewById(R.id.addBtn);
        editBtn = findViewById(R.id.editBtn);
        removeBtn = findViewById(R.id.removeBtn);
        donationRequestsListView = findViewById(R.id.donationRequestsListView);

        setupDummyData();
        setupList();
        setupButtons();
    }

    private void setupDummyData() {
        // Each entry: { itemName, category, quantity }
        allRequests.add(new String[]{"T-Shirts", "Clothing", "10"});
        allRequests.add(new String[]{"Rice", "Food", "5"});
        allRequests.add(new String[]{"Soap", "Toiletries", "20"});
    }

    private void setupList() {
        adapter = new DonationRequestAdapter(this, allRequests);
        donationRequestsListView.setAdapter(adapter);

        // Select a row on tap
        donationRequestsListView.setOnItemClickListener((parent, view, position, id) -> {
            adapter.setSelectedPosition(position);
        });
    }

    private void setupButtons() {

        // Add → go straight to AddOrEditDonationActivity
        addBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddOrEditDonationActivity.class);
            intent.putExtra("mode", "add");
            startActivity(intent);
        });

        // Edit → only if a row is selected
        editBtn.setOnClickListener(v -> {
            if (adapter.getSelectedItem() == null) {
                Toast.makeText(this, "Please select an item to edit", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, AddOrEditDonationActivity.class);
            intent.putExtra("mode", "edit");
            intent.putExtra("itemName", adapter.getSelectedItem()[0]);
            intent.putExtra("quantity", adapter.getSelectedItem()[2]);
            startActivity(intent);
        });

        // Remove → only if a row is selected
        removeBtn.setOnClickListener(v -> {
            if (adapter.getSelectedItem() == null) {
                Toast.makeText(this, "Please select an item to remove", Toast.LENGTH_SHORT).show();
                return;
            }
            String itemName = adapter.getSelectedItem()[0];
            allRequests.remove(adapter.getSelectedPosition());
            adapter.updateData(allRequests);
            Toast.makeText(this, "Removed: " + itemName, Toast.LENGTH_SHORT).show();
        });
    }
}