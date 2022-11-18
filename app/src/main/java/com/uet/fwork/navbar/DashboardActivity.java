package com.uet.fwork.navbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.uet.fwork.R;
import com.uet.fwork.account.profile.ProfileFragment;
import com.uet.fwork.chat.ChatListFragment;
import com.uet.fwork.database.repository.Repository;
import com.uet.fwork.database.repository.UserRepository;

public class DashboardActivity extends AppCompatActivity {

    private UserRepository userRepository;
    FirebaseAuth firebaseAuth;

    BottomNavigationView bottomNavigationView;
    HomeFragment homeFragment = new HomeFragment();
    ProfileFragment profileFragment = new ProfileFragment();
    SearchFragment searchFragment = new SearchFragment();
    NotificationsFragment notificationsFragment = new NotificationsFragment();
    ChatListFragment inboxFragment = new ChatListFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        firebaseAuth = FirebaseAuth.getInstance();

        userRepository = new UserRepository(FirebaseDatabase.getInstance());

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
                        userRepository.getUserRole(firebaseAuth.getUid(), new Repository.OnQuerySuccessListener<String>() {
                            @Override
                            public void onSuccess(String role) {
                                Bundle bundle = new Bundle();
                                bundle.putString("USER_ROLE", role);
                                profileFragment.setArguments(bundle);
                                getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.content, profileFragment)
                                        .commit();
                            }
                        });
                        return true;
                    case R.id.nav_search:
                        getSupportFragmentManager().beginTransaction().replace(R.id.content, searchFragment).commit();
                        return true;
                    case R.id.nav_notifications:
                        getSupportFragmentManager().beginTransaction().replace(R.id.content, notificationsFragment).commit();
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