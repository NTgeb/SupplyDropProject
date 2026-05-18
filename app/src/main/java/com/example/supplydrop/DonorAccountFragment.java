package com.example.supplydrop;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DonorAccountFragment extends Fragment {

    OkHttpClient client = new OkHttpClient();
    String baseUrl = "https://wmc.ms.wits.ac.za/students/sgroup2711/";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_donor_account,
                container, false);

        LinearLayout donationHistoryRow = view.findViewById(R.id.donationHistoryRow);
        LinearLayout donationLeaderBoard = view.findViewById(R.id.donationLeaderBoard);
        LinearLayout logoutRow = view.findViewById(R.id.logoutRow);
        LinearLayout deleteAccountRow    = view.findViewById(R.id.deleteAccountRow);

        donationHistoryRow.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), DonationHistoryActivity.class);
            startActivity(intent);
        });

        donationLeaderBoard.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(),
                    DonationLeaderboardActivity.class);
            startActivity(intent);
        });

        logoutRow.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        deleteAccountRow.setOnClickListener(v -> {
            // Show confirmation dialog first
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete your account? This cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        deleteAccount();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        return view;
    }

    private void deleteAccount() {
        SharedPreferences prefs = requireContext().getSharedPreferences(
                "SupplyDropPrefs", android.content.Context.MODE_PRIVATE);
        int donorId = prefs.getInt("donor_id", -1);

        if (donorId == -1) {
            Toast.makeText(requireContext(),
                    "Error: donor not found",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody requestBody = new FormBody.Builder()
                .add("donor_id", String.valueOf(donorId))
                .build();

        Request request = new Request.Builder()
                .url(baseUrl + "delete_donor_account.php")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(),
                                "Connection failed",
                                Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call,
                                   @NonNull Response response)
                    throws IOException {
                final String body = response.body().string();
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(body);
                        if (obj.getBoolean("success")) {
                            // Clear SharedPreferences
                            requireContext().getSharedPreferences(
                                            "SupplyDropPrefs",
                                            android.content.Context.MODE_PRIVATE)
                                    .edit().clear().apply();

                            Toast.makeText(requireContext(),
                                    "Account deleted",
                                    Toast.LENGTH_SHORT).show();

                            // Go back to login
                            Intent intent = new Intent(requireContext(),
                                    MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(requireContext(),
                                    obj.getString("message"),
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
}