package com.example.supplydrop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DonationHistoryActivity extends AppCompatActivity {

    ListView donationHistoryListView;
    DonationHistoryAdapter adapter;

    List<String[]> allDonations = new ArrayList<>();

    OkHttpClient client = new OkHttpClient();
    String baseUrl = "https://wmc.ms.wits.ac.za/students/sgroup2711/";

    // Store request_id and recipient_id for row click
    List<String[]> donationMeta = new ArrayList<>();

    int donorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_history);

        donationHistoryListView = findViewById(R.id.donationHistoryListView);//Find the List view obj

        //Get donor_id from local storage
        SharedPreferences prefs = getSharedPreferences(
                "SupplyDropPrefs", MODE_PRIVATE);
        donorId = prefs.getInt("donor_id", -1);

        fetchDonationHistory();
    }

    private void fetchDonationHistory() {
        RequestBody requestBody = new FormBody.Builder()
                .add("donor_id", String.valueOf(donorId))
                .build();

        Request request = new Request.Builder()
                .url(baseUrl + "get_donor_history.php")// The donation history php
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(DonationHistoryActivity.this, "Failed to load history", Toast.LENGTH_SHORT).show()
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
                            JSONArray donations = obj.getJSONArray("donations");
                            //Clear any previous lists
                            allDonations.clear();
                            donationMeta.clear();

                            for (int i = 0; i < donations.length(); i++) {//Populate our JSON Objects
                                JSONObject d = donations.getJSONObject(i);
                                String orgName    = d.getString("full_name");
                                String itemName   = d.getString("item_name");
                                String qty        = d.getString("quantity_donated");
                                String requestId  = d.getString("request_id");
                                String recipientId = d.getString("recipient_id");

                                //Add to the lists for table
                                allDonations.add(new String[]{
                                        orgName, itemName, qty
                                });

                                //Add to the list for row clicks
                                donationMeta.add(new String[]{
                                        requestId, recipientId, orgName
                                });
                            }

                            setupList();

                        } else {
                            Toast.makeText(DonationHistoryActivity.this,"No donation history found",Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(DonationHistoryActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void setupList() {
        //Add our populated list to the adapted
        adapter = new DonationHistoryAdapter(this, allDonations);
        donationHistoryListView.setAdapter(adapter);

        //Go to specific donation when clicking on a row
        donationHistoryListView.setOnItemClickListener((parent, view,
                                                        position, id) -> {
            String[] meta = donationMeta.get(position);//Find the info on them
            Intent intent = new Intent(this, SingleRecipientActivity.class);//Calling the activity to display the info
            intent.putExtra("request_id", meta[0]);
            intent.putExtra("recipient_id", meta[1]);
            intent.putExtra("recipient_name", meta[2]);
            startActivity(intent);
        });
    }
}