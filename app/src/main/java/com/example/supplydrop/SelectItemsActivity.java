package com.example.supplydrop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SelectItemsActivity extends AppCompatActivity {

    ExpandableListView expandableListView;
    DonationExpandableAdapter adapter;
    Button confirmBtn;

    OkHttpClient client = new OkHttpClient();
    String baseUrl = "https://wmc.ms.wits.ac.za/students/sgroup2711/";

    // Stores category names in order
    List<String> categoryNames = new ArrayList<>();
    // Stores cat_id for each category name
    Map<String, Integer> categoryIdMap = new HashMap<>();
    // Stores item names per category
    Map<String, List<String>> itemMap = new HashMap<>();
    // Stores item_id for each item name
    Map<String, Integer> itemIdMap = new HashMap<>();

    int recipientId;
    int categoriesLoaded = 0;
    int totalCategories = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_items);

        expandableListView = findViewById(R.id.expandableListView);
        confirmBtn = findViewById(R.id.confirmBtn);

        // Get recipient_id from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(
                "SupplyDropPrefs", MODE_PRIVATE);
        recipientId = prefs.getInt("recipient_id", -1);

        fetchCategories();

        confirmBtn.setOnClickListener(v -> {
            confirmBtn.setEnabled(false);
            List<String> selectedItems = adapter.getSelectedItems();
            if (selectedItems.isEmpty()) {
                Toast.makeText(this,
                        "Please select at least one item",
                        Toast.LENGTH_SHORT).show();
                confirmBtn.setEnabled(true); // re-enable if validation fails
                return;
            }
            saveSelectedItems(selectedItems);
        });
    }

    private void fetchCategories() {
        Request request = new Request.Builder()
                .url(baseUrl + "get_categories.php")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(SelectItemsActivity.this,
                                "Failed to load categories",
                                Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call,
                                   @NonNull Response response) throws IOException {
                final String body = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(body);
                        if (obj.getBoolean("success")) {
                            JSONArray categories = obj.getJSONArray("categories");
                            totalCategories = categories.length();

                            for (int i = 0; i < categories.length(); i++) {
                                JSONObject cat = categories.getJSONObject(i);
                                String catName = cat.getString("cat_name");
                                int catId = cat.getInt("cat_id");

                                categoryNames.add(catName);
                                categoryIdMap.put(catName, catId);
                                itemMap.put(catName, new ArrayList<>());

                                // Fetch items for each category
                                fetchItemsForCategory(catName, catId);
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(SelectItemsActivity.this,
                                "Error loading categories",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void fetchItemsForCategory(String catName, int catId) {
        RequestBody requestBody = new FormBody.Builder()
                .add("cat_id", String.valueOf(catId))
                .build();
        Request request = new Request.Builder()
                .url(baseUrl + "get_items.php")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    categoriesLoaded++;
                    checkIfAllLoaded();
                });
            }

            @Override
            public void onResponse(@NonNull Call call,
                                   @NonNull Response response) throws IOException {
                final String body = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(body);
                        if (obj.getBoolean("success")) {
                            JSONArray items = obj.getJSONArray("items");
                            List<String> itemNames = new ArrayList<>();

                            for (int i = 0; i < items.length(); i++) {
                                JSONObject item = items.getJSONObject(i);
                                String itemName = item.getString("item_name");
                                int itemId = item.getInt("item_id");

                                itemNames.add(itemName);
                                itemIdMap.put(itemName, itemId);
                            }
                            itemMap.put(catName, itemNames);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    categoriesLoaded++;
                    checkIfAllLoaded();
                });
            }
        });
    }

    private void checkIfAllLoaded() {
        // Only set up the adapter once ALL categories and items are loaded
        if (categoriesLoaded == totalCategories) {
            adapter = new DonationExpandableAdapter(
                    this, categoryNames, itemMap);
            expandableListView.setAdapter(adapter);
        }
    }

    private void saveSelectedItems(List<String> selectedItems) {
        // selectedItems format is "Category → ItemName"
        // We need to save each one as a donation_request entry
        final int[] savedCount = {0};
        final int[] failCount = {0};
        int total = selectedItems.size();

        for (String selected : selectedItems) {
            String[] parts = selected.split(" → ");
            if (parts.length < 2) continue;

            String catName = parts[0];
            String itemName = parts[1];

            int catId = categoryIdMap.containsKey(catName)
                    ? categoryIdMap.get(catName) : -1;
            int itemId = itemIdMap.containsKey(itemName)
                    ? itemIdMap.get(itemName) : -1;

            if (catId == -1 || itemId == -1) {
                failCount[0]++;
                continue;
            }

            RequestBody requestBody = new FormBody.Builder()
                    .add("recipient_id", String.valueOf(recipientId))
                    .add("item_name", itemName)
                    .add("cat_id", String.valueOf(catId))
                    .add("item_id", String.valueOf(itemId))
                    .build();

            Request request = new Request.Builder()
                    .url(baseUrl + "save_initial_requests.php")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call,
                                      @NonNull IOException e) {
                    runOnUiThread(() -> {
                        failCount[0]++;
                        checkIfDoneSaving(savedCount[0],
                                failCount[0], total);
                    });
                }

                @Override
                public void onResponse(@NonNull Call call,
                                       @NonNull Response response)
                        throws IOException {
                    runOnUiThread(() -> {
                        savedCount[0]++;
                        checkIfDoneSaving(savedCount[0],
                                failCount[0], total);
                    });
                }
            });
        }
    }

    private void checkIfDoneSaving(int saved, int failed, int total) {
        if (saved + failed == total) {
            if (failed == 0) {
                Toast.makeText(this,
                        "Items saved! Setting up your dashboard...",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        saved + " items saved, " + failed + " failed",
                        Toast.LENGTH_SHORT).show();
            }
            // Navigate to RecipientActivity regardless
            Intent intent = new Intent(
                    SelectItemsActivity.this, RecipientActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}