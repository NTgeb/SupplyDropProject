package com.example.supplydrop;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;

public class RecipientDashboardFragment extends Fragment {

    Button addBtn, editBtn, removeBtn;
    ListView donationRequestsListView;
    List<String[]> allRequests = new ArrayList<>();
    DonationRequestAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_recipient_dashboard, container, false);

        addBtn = view.findViewById(R.id.addBtn);
        editBtn = view.findViewById(R.id.editBtn);
        removeBtn = view.findViewById(R.id.removeBtn);
        donationRequestsListView = view.findViewById(R.id.donationRequestsListView);

        setupDummyData();
        setupList();
        setupButtons();

        return view;
    }

    private void setupDummyData() {
        allRequests.add(new String[]{"T-Shirts", "Clothing", "10"});
        allRequests.add(new String[]{"Rice", "Food", "5"});
        allRequests.add(new String[]{"Soap", "Toiletries", "20"});
    }

    private void setupList() {
        adapter = new DonationRequestAdapter(requireContext(), allRequests);
        donationRequestsListView.setAdapter(adapter);

        donationRequestsListView.setOnItemClickListener((parent, view, position, id) -> {
            adapter.setSelectedPosition(position);
        });
    }

    private void setupButtons() {
        addBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddOrEditDonationActivity.class);
            intent.putExtra("mode", "add");
            startActivity(intent);
        });

        editBtn.setOnClickListener(v -> {
            if (adapter.getSelectedItem() == null) {
                Toast.makeText(requireContext(), "Please select an item to edit", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(requireContext(), AddOrEditDonationActivity.class);
            intent.putExtra("mode", "edit");
            intent.putExtra("itemName", adapter.getSelectedItem()[0]);
            intent.putExtra("quantity", adapter.getSelectedItem()[2]);
            startActivity(intent);
        });

        removeBtn.setOnClickListener(v -> {
            if (adapter.getSelectedItem() == null) {
                Toast.makeText(requireContext(), "Please select an item to remove", Toast.LENGTH_SHORT).show();
                return;
            }
            String itemName = adapter.getSelectedItem()[0];
            allRequests.remove(adapter.getSelectedPosition());
            adapter.updateData(allRequests);
            Toast.makeText(requireContext(), "Removed: " + itemName, Toast.LENGTH_SHORT).show();
        });
    }
}