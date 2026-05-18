package com.example.supplydrop;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
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
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RecipientDashboardFragment extends Fragment {

    Button addBtn, editBtn, removeBtn;
    ListView donationRequestsListView;
    List<String[]> allRequests = new ArrayList<>();
    DonationRequestAdapter adapter;

    OkHttpClient client = new OkHttpClient();
    String baseUrl = "https://wmc.ms.wits.ac.za/students/sgroup2711/";
    int recipientId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_recipient_dashboard,
                container, false);

        addBtn    = view.findViewById(R.id.addBtn);
        editBtn   = view.findViewById(R.id.editBtn);
        removeBtn = view.findViewById(R.id.removeBtn);
        donationRequestsListView = view.findViewById(
                R.id.donationRequestsListView);

        SharedPreferences prefs = requireContext().getSharedPreferences(
                "SupplyDropPrefs", android.content.Context.MODE_PRIVATE);
        recipientId = prefs.getInt("recipient_id", -1);

        setupList();
        setupButtons();
        fetchDonationRequests();

        return view;
    }

    private void fetchDonationRequests() {
        RequestBody requestBody = new FormBody.Builder()
                .add("recipient_id", String.valueOf(recipientId))
                .build();

        Request request = new Request.Builder()
                .url(baseUrl + "get_donations.php")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call,
                                  @NonNull IOException e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(),
                                "Failed to load requests",
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
                            JSONArray donations =
                                    obj.getJSONArray("donations");
                            allRequests.clear();

                            for (int i = 0; i < donations.length(); i++) {
                                JSONObject d = donations.getJSONObject(i);
                                String itemName    = d.getString("item_name");
                                String catName     = d.getString("cat_name");
                                String quantity    = d.getString("quantity");
                                String requestId   = d.getString("request_id");
                                String description = d.optString(
                                        "description", "");
                                String catId       = d.getString("cat_id");
                                String itemImage   = d.optString(
                                        "item_image", "");

                                allRequests.add(new String[]{
                                        itemName, catName,
                                        quantity, requestId,
                                        description, catId, itemImage
                                });
                            }
                            adapter.updateData(allRequests);

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

    private void setupList() {
        adapter = new DonationRequestAdapter(requireContext(), allRequests);
        donationRequestsListView.setAdapter(adapter);

        donationRequestsListView.setOnItemClickListener((parent, view,
                                                         position, id) -> {
            adapter.setSelectedPosition(position);
        });
    }

    private void setupButtons() {
        addBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(),
                    AddOrEditDonationActivity.class);
            intent.putExtra("mode", "add");
            intent.putExtra("recipient_id", recipientId);
            startActivity(intent);
        });

        editBtn.setOnClickListener(v -> {
            if (adapter.getSelectedItem() == null) {
                Toast.makeText(requireContext(),
                        "Please select an item to edit",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            String[] selected = adapter.getSelectedItem();
            Intent intent = new Intent(requireContext(),
                    AddOrEditDonationActivity.class);
            intent.putExtra("mode", "edit");
            intent.putExtra("itemName", selected[0]);
            intent.putExtra("quantity", selected[2]);
            intent.putExtra("request_id", Integer.parseInt(selected[3]));
            intent.putExtra("description", selected[4]);
            intent.putExtra("cat_id", Integer.parseInt(selected[5]));
            intent.putExtra("item_image", selected[6]);
            intent.putExtra("recipient_id", recipientId);
            startActivity(intent);
        });

        removeBtn.setOnClickListener(v -> {
            if (adapter.getSelectedItem() == null) {
                Toast.makeText(requireContext(),
                        "Please select an item to remove",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            String itemName  = adapter.getSelectedItem()[0];
            String requestId = adapter.getSelectedItem()[3];

            new AlertDialog.Builder(requireContext())
                    .setTitle("Remove Request")
                    .setMessage("Are you sure you want to remove \""
                            + itemName + "\"? This cannot be undone.")
                    .setPositiveButton("Remove", (dialog, which) ->
                            deleteRequest(requestId, itemName))
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void deleteRequest(String requestId, String itemName) {
        RequestBody requestBody = new FormBody.Builder()
                .add("request_id", requestId)
                .add("recipient_id", String.valueOf(recipientId))
                .build();

        Request request = new Request.Builder()
                .url(baseUrl + "delete_request.php")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call,
                                  @NonNull IOException e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(),
                                "Failed to delete",
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
                            Toast.makeText(requireContext(),
                                    "Removed: " + itemName,
                                    Toast.LENGTH_SHORT).show();
                            fetchDonationRequests();
                        } else {
                            Toast.makeText(requireContext(),
                                    "Failed: " + obj.getString("message"),
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

    @Override
    public void onResume() {
        super.onResume();
        fetchDonationRequests();
    }
}