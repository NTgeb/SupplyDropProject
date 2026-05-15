package com.example.supplydrop;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DonorDashboardFragment extends Fragment {

    Spinner categorySpinner, areaSpinner;
    Button clearBtn;
    ListView recipientListView;

    List<String[]> allRecipients = new ArrayList<>();
    List<String[]> filteredRecipients = new ArrayList<>();

    List<String> categoryList = new ArrayList<>();
    List<String> areaList = new ArrayList<>();

    ArrayAdapter<String> categoryAdapter, areaAdapter;
    OkHttpClient client = new OkHttpClient();

    String baseUrl = "https://wmc.ms.wits.ac.za/students/sgroup2711/";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_home, container, false);

        categorySpinner = view.findViewById(R.id.categorySpinner);
        areaSpinner = view.findViewById(R.id.areaSpinner);
        clearBtn = view.findViewById(R.id.clearBtn);
        recipientListView = view.findViewById(R.id.recipientListView);

        // Hide profile icon since account is in bottom nav
        ImageButton profileIcon = view.findViewById(R.id.profileIcon);
        profileIcon.setVisibility(View.GONE);

        setupSpinners();
        setupClearButton();
        fetchRecipients();

        return view;
    }

    private void setupSpinners() {
        // Category spinner
        categoryList.add("All Categories");
        categoryAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        // Area spinner
        areaList.add("All Areas");
        areaAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, areaList);
        areaAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        areaSpinner.setAdapter(areaAdapter);

        AdapterView.OnItemSelectedListener filterListener =
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int position, long id) {
                        applyFilters();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                };

        categorySpinner.setOnItemSelectedListener(filterListener);
        areaSpinner.setOnItemSelectedListener(filterListener);

        // Fetch categories from DB to populate category spinner
        fetchCategories();
    }

    private void fetchCategories() {
        Request request = new Request.Builder()
                .url(baseUrl + "get_categories.php")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Keep "All Categories" only if it fails
            }

            @Override
            public void onResponse(@NonNull Call call,
                                   @NonNull Response response) throws IOException {
                final String body = response.body().string();
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(body);
                        if (obj.getBoolean("success")) {
                            JSONArray categories = obj.getJSONArray("categories");
                            for (int i = 0; i < categories.length(); i++) {
                                JSONObject cat = categories.getJSONObject(i);
                                categoryList.add(cat.getString("cat_name"));
                            }
                            categoryAdapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    private void fetchRecipients() {
        Request request = new Request.Builder()
                .url(baseUrl + "get_recipients.php")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(),
                                "Failed to load recipients",
                                Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call,
                                   @NonNull Response response) throws IOException {
                final String body = response.body().string();
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(body);
                        if (obj.getBoolean("success")) {
                            JSONArray recipients = obj.getJSONArray("recipients");
                            allRecipients.clear();
                            areaList.clear();
                            areaList.add("All Areas");

                            for (int i = 0; i < recipients.length(); i++) {
                                JSONObject r = recipients.getJSONObject(i);
                                String name = r.getString("full_name");
                                String city = r.getString("city");
                                String categories = r.optString("categories", "");
                                String recipientId = r.getString("recipient_id");

                                // { name, categories, city, recipientId }
                                allRecipients.add(new String[]{
                                        name, categories, city, recipientId
                                });

                                // Add city to area spinner if not already there
                                if (!areaList.contains(city)) {
                                    areaList.add(city);
                                }
                            }

                            filteredRecipients.addAll(allRecipients);
                            areaAdapter.notifyDataSetChanged();
                            setupRecipientList();

                        } else {
                            Toast.makeText(requireContext(),
                                    "No recipients found",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(requireContext(),
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void applyFilters() {
        String selectedCategory = categorySpinner.getSelectedItem().toString();
        String selectedArea = areaSpinner.getSelectedItem().toString();

        filteredRecipients.clear();
        for (String[] recipient : allRecipients) {
            boolean categoryMatch = selectedCategory.equals("All Categories")
                    || recipient[1].contains(selectedCategory);
            boolean areaMatch = selectedArea.equals("All Areas")
                    || recipient[2].equals(selectedArea);
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
            Intent intent = new Intent(requireContext(),
                    SingleRecipientActivity.class);
            intent.putExtra("recipient_id", recipient[3]);
            intent.putExtra("recipient_name", recipient[0]);
            startActivity(intent);
        });
    }

    private void updateListView() {
        if (recipientListView.getAdapter() == null) {
            RecipientAdapter adapter = new RecipientAdapter(
                    requireContext(), filteredRecipients);
            recipientListView.setAdapter(adapter);
        } else {
            ((RecipientAdapter) recipientListView.getAdapter())
                    .updateData(filteredRecipients);
        }
    }

    private void setupClearButton() {
        clearBtn.setOnClickListener(v -> {
            categorySpinner.setSelection(0);
            areaSpinner.setSelection(0);
        });
    }
}