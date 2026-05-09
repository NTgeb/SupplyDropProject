package com.example.supplydrop;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;

public class DonorDashboardFragment extends Fragment {

    Spinner categorySpinner, areaSpinner;
    Button clearBtn;
    ListView recipientListView;
    List<String[]> allRecipients = new ArrayList<>();
    List<String[]> filteredRecipients = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_home, container, false);

        categorySpinner = view.findViewById(R.id.categorySpinner);
        areaSpinner = view.findViewById(R.id.areaSpinner);
        clearBtn = view.findViewById(R.id.clearBtn);
        recipientListView = view.findViewById(R.id.recipientListView);

        // Remove profile icon click since account is now in bottom nav
        ImageButton profileIcon = view.findViewById(R.id.profileIcon);
        profileIcon.setVisibility(View.GONE);

        setupDummyData();
        setupSpinners();
        setupRecipientList();
        setupClearButton();

        return view;
    }

    private void setupDummyData() {
        allRecipients.add(new String[]{"Hope Foundation", "Clothing, Food", "Johannesburg", "Clothing"});
        allRecipients.add(new String[]{"Ubuntu Centre", "Toiletries", "Cape Town", "Toiletries"});
        allRecipients.add(new String[]{"Helping Hands", "Food, Toiletries", "Durban", "Food"});
        allRecipients.add(new String[]{"Care Network", "Clothing", "Johannesburg", "Clothing"});
        filteredRecipients.addAll(allRecipients);
    }

    private void setupSpinners() {
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"All Categories", "Clothing", "Food", "Toiletries"});
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        ArrayAdapter<String> areaAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"All Areas", "Johannesburg", "Cape Town", "Durban"});
        areaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        areaSpinner.setAdapter(areaAdapter);

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
        recipientListView.setOnItemClickListener((parent, view, position, id) -> {
            String[] recipient = filteredRecipients.get(position);
            Toast.makeText(requireContext(), "Opening: " + recipient[0], Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(requireContext(), SingleRecipientActivity.class);
            // intent.putExtra("recipientName", recipient[0]);
            // startActivity(intent);
        });
    }

    private void updateListView() {
        if (recipientListView.getAdapter() == null) {
            RecipientAdapter adapter = new RecipientAdapter(requireContext(), filteredRecipients);
            recipientListView.setAdapter(adapter);
        } else {
            ((RecipientAdapter) recipientListView.getAdapter()).updateData(filteredRecipients);
        }
    }

    private void setupClearButton() {
        clearBtn.setOnClickListener(v -> {
            categorySpinner.setSelection(0);
            areaSpinner.setSelection(0);
        });
    }
}