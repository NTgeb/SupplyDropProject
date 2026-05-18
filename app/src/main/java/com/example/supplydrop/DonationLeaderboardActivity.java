package com.example.supplydrop;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DonationLeaderboardActivity extends AppCompatActivity {

    ListView leaderboardListView;
    EditText usernameSearch;
    Button clearBtn;
    LeaderboardAdapter adapter;

    List<String[]> allDonors = new ArrayList<>();
    List<String[]> filteredDonors = new ArrayList<>();

    OkHttpClient client = new OkHttpClient();
    String baseUrl = "https://wmc.ms.wits.ac.za/students/sgroup2711/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_leaderboard);

        leaderboardListView = findViewById(R.id.leaderboardListView);
        usernameSearch      = findViewById(R.id.usernameSearch);
        clearBtn            = findViewById(R.id.clearBtn);

        setupSearch();
        setupClearButton();
        fetchLeaderboard();
    }

    private void fetchLeaderboard() {
        Request request = new Request.Builder()
                .url(baseUrl + "get_leaderboard.php")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(DonationLeaderboardActivity.this,
                                "Failed to load leaderboard",
                                Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call,
                                   @NonNull Response response)
                    throws IOException {
                final String body = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(body);
                        if (obj.getBoolean("success")) {
                            JSONArray donors = obj.getJSONArray("donors");
                            allDonors.clear();
                            filteredDonors.clear();

                            for (int i = 0; i < donors.length(); i++) {
                                JSONObject d = donors.getJSONObject(i);
                                String position      = String.valueOf(i + 1);
                                String username      = d.getString("username");
                                String donationCount = d.getString(
                                        "donation_count");

                                allDonors.add(new String[]{
                                        position, username, donationCount
                                });
                            }

                            filteredDonors.addAll(allDonors);
                            setupList();

                        } else {
                            Toast.makeText(DonationLeaderboardActivity.this,
                                    "No data found",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(DonationLeaderboardActivity.this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void setupList() {
        adapter = new LeaderboardAdapter(this, filteredDonors);
        leaderboardListView.setAdapter(adapter);
    }

    private void setupSearch() {
        usernameSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                applyFilter(s.toString().trim().toLowerCase());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void applyFilter(String query) {
        filteredDonors.clear();
        int position = 1;
        for (String[] donor : allDonors) {
            if (query.isEmpty() || donor[1].toLowerCase().contains(query)) {
                // Re-number positions based on filtered results
                filteredDonors.add(new String[]{
                        String.valueOf(position), donor[1], donor[2]
                });
                position++;
            }
        }
        if (adapter != null) {
            adapter.updateData(filteredDonors);
        }
    }

    private void setupClearButton() {
        clearBtn.setOnClickListener(v -> {
            usernameSearch.setText("");
        });
    }
}