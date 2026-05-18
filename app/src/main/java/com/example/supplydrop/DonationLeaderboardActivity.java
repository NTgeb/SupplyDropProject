package com.example.supplydrop;

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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DonationLeaderboardActivity extends AppCompatActivity {

    ListView leaderboardListView;
    LeaderboardAdapter adapter;

    List<String[]> allDonors = new ArrayList<>();

    OkHttpClient client = new OkHttpClient();
    String baseUrl = "https://wmc.ms.wits.ac.za/students/sgroup2711/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_leaderboard);

        leaderboardListView = findViewById(R.id.leaderboardListView);

        fetchLeaderboard();
    }

    private void fetchLeaderboard() {
        Request request = new Request.Builder()
                .url(baseUrl + "get_leaderboard.php")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call,
                                  @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(DonationLeaderboardActivity.this,
                                "Failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call,
                                   @NonNull Response response)
                    throws IOException {
                if (response.body() == null) return;
                final String body = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(body);
                        if (obj.getBoolean("success")) {
                            JSONArray donors = obj.getJSONArray("donors");
                            allDonors.clear();

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

                            adapter = new LeaderboardAdapter(
                                    DonationLeaderboardActivity.this,
                                    allDonors);
                            leaderboardListView.setAdapter(adapter);

                        } else {
                            Toast.makeText(DonationLeaderboardActivity.this,
                                    "No data found",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(DonationLeaderboardActivity.this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}