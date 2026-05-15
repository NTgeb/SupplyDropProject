package com.example.supplydrop;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.activity.OnBackPressedCallback;

public class DonorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor);

        BottomNavigationView bottomNav = findViewById(R.id.donorBottomNav);

        // Load dashboard fragment by default
        loadFragment(new DonorDashboardFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected;
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                selected = new DonorDashboardFragment();
            } else if (id == R.id.nav_account) {
                selected = new DonorAccountFragment();
            } else {
                return false;
            }

            loadFragment(selected);
            return true;
        });
        getOnBackPressedDispatcher().addCallback(this,
                new androidx.activity.OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        BottomNavigationView bottomNav = findViewById(R.id.donorBottomNav);
                        if (bottomNav.getSelectedItemId() == R.id.nav_account) {
                            // On account → go to dashboard
                            bottomNav.setSelectedItemId(R.id.nav_dashboard);
                        }
                        // On dashboard → do nothing (back is blocked)
                    }
                });
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.donorFragmentContainer, fragment);
        ft.commit();
    }
}