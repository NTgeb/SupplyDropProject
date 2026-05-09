package com.example.supplydrop;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.fragment.app.Fragment;

public class DonorAccountFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_donor_account, container, false);

        LinearLayout donationHistoryRow = view.findViewById(R.id.donationHistoryRow);
        LinearLayout logoutRow = view.findViewById(R.id.logoutRow);

        donationHistoryRow.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), DonationHistoryActivity.class);
            startActivity(intent);
        });

        logoutRow.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }
}