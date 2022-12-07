package com.uet.fwork.navbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.uet.fwork.R;
import com.uet.fwork.account.profile.ProfileFragment;
import com.uet.fwork.chat.ChatListFragment;
import com.uet.fwork.database.model.UserRole;
import com.uet.fwork.database.repository.Repository;
import com.uet.fwork.database.repository.UserRepository;
import com.uet.fwork.firebasehelper.FirebaseAuthHelper;
import com.uet.fwork.notification.NotificationFragment;

public class DashboardActivity extends AppCompatActivity {

    private static final String LOG_TAG = "Dashboard activity";

    private UserRepository userRepository;
    private FirebaseAuthHelper firebaseAuthHelper;

    FirebaseAuth firebaseAuth;

    BottomNavigationView bottomNavigationView;
    HomeFragment homeFragment = new HomeFragment();
    private CandidateDashboardFragment candidateDashboardFragment = new CandidateDashboardFragment();
    private EmployerDashboardFragment employerDashboardFragment = new EmployerDashboardFragment();
    private NotificationFragment notificationFragment = new NotificationFragment();
    ProfileFragment profileFragment = new ProfileFragment();
    SearchFragment searchFragment = new SearchFragment();
    ChatListFragment inboxFragment = new ChatListFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuthHelper = FirebaseAuthHelper.getInstance();

        userRepository = UserRepository.getInstance();

        bottomNavigationView = findViewById(R.id.navigation);
        getSupportFragmentManager().beginTransaction().replace(R.id.content, homeFragment).commit();

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        getSupportFragmentManager().beginTransaction().replace(R.id.content, homeFragment).commit();
                        return true;
                    case R.id.nav_profile:
                        String userRole = firebaseAuthHelper.getUser().getRole();
                        if (userRole != null) {
                            if (userRole.equals(UserRole.CANDIDATE)) {
                                getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.content, candidateDashboardFragment)
                                        .commit();
                            } else if (userRole.equals(UserRole.EMPLOYER)) {
                                getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.content, employerDashboardFragment)
                                        .commit();
                            }
                        } else {
                            Log.d(LOG_TAG, "User role is null");
                        }
                        return true;
                    case R.id.nav_search:
                        getSupportFragmentManager().beginTransaction().replace(R.id.content, searchFragment).commit();
                        return true;
                    case R.id.nav_notifications:
                        getSupportFragmentManager().beginTransaction().replace(R.id.content, notificationFragment).commit();
                        return true;
                    case R.id.nav_inbox:
                        getSupportFragmentManager().beginTransaction().replace(R.id.content, inboxFragment).commit();
                        return true;
                }
                return false;
            }
        });

    }
}