package com.example.supplydrop;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

    Spinner categorySpinner, areaSpinner, itemSpinner;
    EditText recipientSearch;
    Button clearBtn;
    ListView recipientListView;

    List<String[]> allRequests = new ArrayList<>();
    List<String[]> filteredRequests = new ArrayList<>();

    List<String> categoryList = new ArrayList<>();
    List<String> areaList = new ArrayList<>();
    List<String> itemList = new ArrayList<>();

    ArrayAdapter<String> categoryAdapter, areaAdapter, itemAdapter;
    OkHttpClient client = new OkHttpClient();

    String baseUrl = "https://wmc.ms.wits.ac.za/students/sgroup2711/";
    String filterRecipientName = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_home, container, false);

        categorySpinner  = view.findViewById(R.id.categorySpinner);
        areaSpinner      = view.findViewById(R.id.areaSpinner);
        itemSpinner      = view.findViewById(R.id.itemSpinner);
        recipientSearch  = view.findViewById(R.id.recipientSearch);
        clearBtn         = view.findViewById(R.id.clearBtn);
        recipientListView = view.findViewById(R.id.recipientListView);

        ImageButton profileIcon = view.findViewById(R.id.profileIcon);
        profileIcon.setVisibility(View.GONE);

        // Check if coming from View Other Donations on profile screen
        if (getActivity() != null) {
            filterRecipientName = getActivity().getIntent()
                    .getStringExtra("filter_recipient");
        }

        setupSpinners();
        setupClearButton();
        fetchAllRequests();

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

        // Item spinner
        itemList.add("All Items");
        itemAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, itemList);
        itemAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        itemSpinner.setAdapter(itemAdapter);

        // Spinner filter listener
        AdapterView.OnItemSelectedListener filterListener =
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int position,
                                               long id) {
                        applyFilters();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                };

        categorySpinner.setOnItemSelectedListener(filterListener);
        areaSpinner.setOnItemSelectedListener(filterListener);
        itemSpinner.setOnItemSelectedListener(filterListener);

        // Recipient text search filter
        recipientSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                applyFilters();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchAllRequests() {
        Request request = new Request.Builder()
                .url(baseUrl + "get_all_requests.php")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(),
                                "Failed to load requests",
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
                            JSONArray requests = obj.getJSONArray("requests");
                            allRequests.clear();
                            areaList.clear();
                            areaList.add("All Areas");
                            categoryList.clear();
                            categoryList.add("All Categories");
                            itemList.clear();
                            itemList.add("All Items");

                            for (int i = 0; i < requests.length(); i++) {
                                JSONObject r = requests.getJSONObject(i);
                                String recipientName = r.getString("full_name");
                                String itemName      = r.getString("item_name");
                                String city          = r.getString("city");
                                String catName       = r.getString("cat_name");
                                String requestId     = r.getString("request_id");
                                String recipientId   = r.getString("recipient_id");

                                // { recipientName, itemName, city,
                                //   catName, requestId, recipientId }
                                allRequests.add(new String[]{
                                        recipientName, itemName,
                                        city, catName,
                                        requestId, recipientId
                                });

                                if (!areaList.contains(city))
                                    areaList.add(city);
                                if (!categoryList.contains(catName))
                                    categoryList.add(catName);
                                if (!itemList.contains(itemName))
                                    itemList.add(itemName);
                            }

                            categoryAdapter.notifyDataSetChanged();
                            areaAdapter.notifyDataSetChanged();
                            itemAdapter.notifyDataSetChanged();

                            // Auto-fill search if coming from profile screen
                            if (filterRecipientName != null) {
                                recipientSearch.setText(filterRecipientName);
                                filterRecipientName = null;
                            }

                            filteredRequests.addAll(allRequests);
                            setupRequestList();

                        } else {
                            Toast.makeText(requireContext(),
                                    "No requests found",
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
        if (categorySpinner.getSelectedItem() == null) return;

        String selectedCategory = categorySpinner.getSelectedItem().toString();
        String selectedArea     = areaSpinner.getSelectedItem().toString();
        String selectedItem     = itemSpinner.getSelectedItem().toString();
        String searchedRecipient = recipientSearch.getText()
                .toString().trim().toLowerCase();

        filteredRequests.clear();
        for (String[] request : allRequests) {
            boolean categoryMatch = selectedCategory.equals("All Categories")
                    || request[3].equals(selectedCategory);
            boolean areaMatch     = selectedArea.equals("All Areas")
                    || request[2].equals(selectedArea);
            boolean itemMatch     = selectedItem.equals("All Items")
                    || request[1].equals(selectedItem);
            boolean recipientMatch = searchedRecipient.isEmpty()
                    || request[0].toLowerCase().contains(searchedRecipient);

            if (categoryMatch && areaMatch && itemMatch && recipientMatch) {
                filteredRequests.add(request);
            }
        }
        updateListView();
    }

    private void setupRequestList() {
        updateListView();
        recipientListView.setOnItemClickListener((parent, view,
                                                  position, id) -> {
            String[] request = filteredRequests.get(position);
            Intent intent = new Intent(requireContext(),
                    SingleRecipientActivity.class);
            intent.putExtra("request_id", request[4]);
            intent.putExtra("recipient_id", request[5]);
            intent.putExtra("recipient_name", request[0]);
            startActivity(intent);
        });
    }

    private void updateListView() {
        if (recipientListView.getAdapter() == null) {
            RecipientAdapter adapter = new RecipientAdapter(
                    requireContext(), filteredRequests);
            recipientListView.setAdapter(adapter);
        } else {
            ((RecipientAdapter) recipientListView.getAdapter())
                    .updateData(filteredRequests);
        }
    }

    private void setupClearButton() {
        clearBtn.setOnClickListener(v -> {
            categorySpinner.setSelection(0);
            areaSpinner.setSelection(0);
            itemSpinner.setSelection(0);
            recipientSearch.setText("");
        });
    }
}