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

    Button addBtn, removeBtn;
    ListView donationRequestsListView;
    List<String[]> allRequests = new ArrayList<>();
    DonationRequestAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_recipient_dashboard, container, false);

        addBtn = view.findViewById(R.id.addBtn);
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
            // Get the clicked item's data
            String[] clickedItem = allRequests.get(position);

            // Open AddOrEditDonationActivity in edit mode
            // and pass the existing data to pre-fill the fields
            Intent intent = new Intent(requireContext(), AddOrEditDonationActivity.class);
            intent.putExtra("mode", "edit");
            intent.putExtra("itemName", clickedItem[0]);
            intent.putExtra("quantity", clickedItem[2]);
            startActivity(intent);
        });
    }

    private void setupButtons() {
        addBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddOrEditDonationActivity.class);
            intent.putExtra("mode", "add");
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