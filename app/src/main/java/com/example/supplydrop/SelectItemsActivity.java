package com.example.supplydrop;

import android.os.Bundle;
import android.widget.ExpandableListView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectItemsActivity extends AppCompatActivity {

    ExpandableListView expandableListView;
    DonationExpandableAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_items);

        expandableListView = findViewById(R.id.expandableListView);

        // Dummy data for now
        List<String> categories = new ArrayList<>();
        categories.add("Clothing");
        categories.add("Food");
        categories.add("Toiletries");

        Map<String, List<String>> itemMap = new HashMap<>();
        itemMap.put("Clothing", Arrays.asList("T-Shirts", "Jackets", "Shoes", "Socks"));
        itemMap.put("Food", Arrays.asList("Canned Beans", "Rice", "Pasta", "Bread"));
        itemMap.put("Toiletries", Arrays.asList("Soap", "Toothbrush", "Shampoo"));

        adapter = new DonationExpandableAdapter(this, categories, itemMap);
        expandableListView.setAdapter(adapter);
    }
}