package com.example.supplydrop;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    Spinner categorySpinner, areaSpinner;
    Button clearBtn;
    ImageButton profileIcon;
    ListView recipientListView;

    // Dummy recipient data - replace with DB later
    List<String[]> allRecipients = new ArrayList<>();
    List<String[]> filteredRecipients = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        categorySpinner = findViewById(R.id.categorySpinner);
        areaSpinner = findViewById(R.id.areaSpinner);
        clearBtn = findViewById(R.id.clearBtn);
        profileIcon = findViewById(R.id.profileIcon);
        recipientListView = findViewById(R.id.recipientListView);

        setupDummyData();
        setupSpinners();
        setupRecipientList();
        setupProfileMenu();
        setupClearButton();
    }

    private void setupDummyData() {
        // Each entry: { name, items, area, category }
        allRecipients.add(new String[]{"Hope Foundation", "Clothing, Food", "Johannesburg", "Clothing"});
        allRecipients.add(new String[]{"Ubuntu Centre", "Toiletries", "Cape Town", "Toiletries"});
        allRecipients.add(new String[]{"Helping Hands", "Food, Toiletries", "Durban", "Food"});
        allRecipients.add(new String[]{"Care Network", "Clothing", "Johannesburg", "Clothing"});
        filteredRecipients.addAll(allRecipients);
    }

    private void setupSpinners() {
        // Category spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"All Categories", "Clothing", "Food", "Toiletries"});
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        // Area spinner
        ArrayAdapter<String> areaAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"All Areas", "Johannesburg", "Cape Town", "Durban"});
        areaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        areaSpinner.setAdapter(areaAdapter);

        // Apply filter when either spinner changes
        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        categorySpinner.setOnItemSelectedListener(filterListener);
        areaSpinner.setOnItemSelectedListener(filterListener);
    }

    private void applyFilters() {
        String selectedCategory = categorySpinner.getSelectedItem().toString();
        String selectedArea = areaSpinner.getSelectedItem().toString();

        filteredRecipients.clear();
        for (String[] recipient : allRecipients) {
            boolean categoryMatch = selectedCategory.equals("All Categories") || recipient[3].equals(selectedCategory);
            boolean areaMatch = selectedArea.equals("All Areas") || recipient[2].equals(selectedArea);
            if (categoryMatch && areaMatch) {
                filteredRecipients.add(recipient);
            }
        }
        updateListView();
    }

    private void setupRecipientList() {
        updateListView();

        // Click on a recipient row → go to their page
        recipientListView.setOnItemClickListener((parent, view, position, id) -> {
            String[] recipient = filteredRecipients.get(position);
            // TODO: pass recipient name to their profile screen
            Toast.makeText(this, "Opening: " + recipient[0], Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(this, RecipientProfileActivity.class);
            // intent.putExtra("recipientName", recipient[0]);
            // startActivity(intent);
        });
    }

    private void updateListView() {
        if (recipientListView.getAdapter() == null) {
            RecipientAdapter recipientAdapter = new RecipientAdapter(this, filteredRecipients);
            recipientListView.setAdapter(recipientAdapter);
        } else {
            ((RecipientAdapter) recipientListView.getAdapter()).updateData(filteredRecipients);
        }
    }

    private void setupProfileMenu() {
        profileIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, profileIcon);
            popup.getMenu().add("Account Info");
            popup.getMenu().add("Donation History");
            popup.getMenu().add("Logout");

            popup.setOnMenuItemClickListener(item -> {
                switch (item.getTitle().toString()) {
                    case "Account Info":
                        Toast.makeText(this, "Account Info - coming soon", Toast.LENGTH_SHORT).show();
                        return true;
                    case "Donation History":
                        Toast.makeText(this, "Donation History - coming soon", Toast.LENGTH_SHORT).show();
                        return true;
                    case "Logout":
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        return true;
                }
                return false;
            });
            popup.show();
        });
    }

    private void setupClearButton() {
        clearBtn.setOnClickListener(v -> {
            categorySpinner.setSelection(0);
            areaSpinner.setSelection(0);
        });
    }
}