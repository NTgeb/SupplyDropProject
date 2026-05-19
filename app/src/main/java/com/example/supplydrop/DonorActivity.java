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

        // Load the dahsboard by default
        loadFragment(new DonorDashboardFragment());

        bottomNav.setOnItemSelectedListener(item -> { // Listener set to track swithces
            Fragment selected;
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {//Assign the selected fragment
                selected = new DonorDashboardFragment();
            } else if (id == R.id.nav_account) {
                selected = new DonorAccountFragment();
            } else {
                return false;
            }

            loadFragment(selected);//Load the new screen to th fragment
            return true;
        });
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            //Creates the queue to backtrack when the back button is pressed
            @Override
            public void handleOnBackPressed() {
                BottomNavigationView bottomNav = findViewById(R.id.donorBottomNav);
                if (bottomNav.getSelectedItemId() == R.id.nav_account) {
                    // Go to Dashboard
                    bottomNav.setSelectedItemId(R.id.nav_dashboard);
                }//Dont need to do anything else otherwise
            }
        });
    }

    private void loadFragment(Fragment fragment) {//Load fragment function
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.donorFragmentContainer, fragment);
        ft.commit();
    }
}