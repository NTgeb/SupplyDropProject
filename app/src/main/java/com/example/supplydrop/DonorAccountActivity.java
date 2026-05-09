package com.example.supplydrop;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class DonorAccountActivity extends AppCompatActivity {

    LinearLayout donationHistoryRow, logoutRow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_account);

        donationHistoryRow = findViewById(R.id.donationHistoryRow);
        logoutRow = findViewById(R.id.logoutRow);

        donationHistoryRow.setOnClickListener(v -> {
            Intent intent = new Intent(this, DonationHistoryActivity.class);
            startActivity(intent);
        });

        logoutRow.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}