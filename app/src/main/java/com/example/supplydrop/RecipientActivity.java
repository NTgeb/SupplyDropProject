package com.example.supplydrop;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.activity.OnBackPressedCallback;

public class RecipientActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipient);

        BottomNavigationView bottomNav = findViewById(R.id.recipientBottomNav);
        //this is the bottom bar to switch betw the frags

        // loads dashboard frag by default so its first
        loadFragment(new RecipientDashboardFragment());

        bottomNav.setOnItemSelectedListener(item ->
        {
            Fragment selected;
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) // creates frag object if they click the dashboard button
            {
                selected = new RecipientDashboardFragment();
            }
            else if (id == R.id.nav_account) // creates frag object if they clixk the account button
            {
                selected = new RecipientAccountFragment();
            }
            else
            {
                return false;
            }

            loadFragment(selected); // moves betw the frags
            return true;
        });

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true)
        {
                    @Override
                    public void handleOnBackPressed()
                    {
                        BottomNavigationView bottomNav = findViewById(R.id.recipientBottomNav);
                        if (bottomNav.getSelectedItemId() == R.id.nav_account)
                        {
                            // if they use back button on phone takes them from account back to dashboard
                            bottomNav.setSelectedItemId(R.id.nav_dashboard);
                        }
                        // if on dashboard they can't go back(back button does nothing)
                    }
        });
    }

    private void loadFragment(Fragment fragment) // switches frags
    {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.recipientFragmentContainer, fragment);
        ft.commit();
    }
}