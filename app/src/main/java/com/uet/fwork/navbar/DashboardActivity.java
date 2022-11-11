package com.uet.fwork.navbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.uet.fwork.R;

public class DashboardActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    HomeFragment homeFragment = new HomeFragment();
    ProfileFragment profileFragment = new ProfileFragment();
    SearchFragment searchFragment = new SearchFragment();
    NotificationsFragment notificationsFragment = new NotificationsFragment();
    InboxFragment inboxFragment = new InboxFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        bottomNavigationView = findViewById(R.id.navigation);
        getSupportFragmentManager().beginTransaction().replace(R.id.content, homeFragment).commit();

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected( MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_home:
                        getSupportFragmentManager().beginTransaction().replace(R.id.content,homeFragment).commit();
                        return true;
                    case R.id.nav_profile:
                        getSupportFragmentManager().beginTransaction().replace(R.id.content,profileFragment).commit();
                        return true;
                    case R.id.nav_search:
                        getSupportFragmentManager().beginTransaction().replace(R.id.content, searchFragment).commit();
                        return true;
                    case R.id.nav_notifications:
                        getSupportFragmentManager().beginTransaction().replace(R.id.content,notificationsFragment).commit();
                        return true;
                    case R.id.nav_inbox:
                        getSupportFragmentManager().beginTransaction().replace(R.id.content,inboxFragment).commit();
                        return true;
                }
                return false;
            }
        });

    }

}