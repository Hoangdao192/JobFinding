package com.uet.fwork.navbar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.uet.fwork.R;
import com.uet.fwork.account.profile.ProfileFragment;
import com.uet.fwork.chat.ChatListFragment;
import com.uet.fwork.database.model.UserRole;
import com.uet.fwork.database.repository.UserRepository;
import com.uet.fwork.firebasehelper.FirebaseAuthHelper;
import com.uet.fwork.notification.NotificationFragment;
import com.uet.fwork.post.AddPostActivity;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private static final String LOG_TAG = "Dashboard activity";

    private UserRepository userRepository;
    private FirebaseAuthHelper firebaseAuthHelper;

    private ImageView btnHome, btnAdd, btnChat, btnDashboard;
    private int selectedButtonId;
    List<ImageView> btnList;

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
        getSupportFragmentManager().beginTransaction().replace(R.id.content, homeFragment).commit();

        btnHome = (ImageView) findViewById(R.id.btnHome);
        btnAdd = (ImageView) findViewById(R.id.btnAdd);
        btnChat = (ImageView) findViewById(R.id.btnChat);
        btnDashboard = (ImageView) findViewById(R.id.btnDashboard);

        selectedButtonId = btnHome.getId();

        btnHome.setOnClickListener(imgView -> {
            selectedButtonId = R.id.btnHome;
            updateSelectedItem();
            getSupportFragmentManager().beginTransaction().replace(R.id.content, homeFragment).commit();
        });
        btnAdd.setOnClickListener(imgView -> {
            selectedButtonId = R.id.btnAdd;
            updateSelectedItem();
            startActivity(new Intent(this, AddPostActivity.class));
        });
        btnChat.setOnClickListener(imgView -> {
            selectedButtonId = R.id.btnChat;
            updateSelectedItem();
            getSupportFragmentManager().beginTransaction().replace(R.id.content, inboxFragment).commit();
        });
        btnDashboard.setOnClickListener(imgView -> {
            selectedButtonId = R.id.btnDashboard;
            updateSelectedItem();
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
        });

        btnList = new ArrayList<>();
        btnList.add(btnHome);
        btnList.add(btnChat);
        btnList.add(btnDashboard);

//        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(MenuItem item) {
//                switch (item.getItemId()) {
//                    case R.id.nav_home:
//
//                        return true;
//                    case R.id.nav_profile:
//                        String userRole = firebaseAuthHelper.getUser().getRole();
//                        if (userRole != null) {
//                            if (userRole.equals(UserRole.CANDIDATE)) {
//                                getSupportFragmentManager()
//                                        .beginTransaction()
//                                        .replace(R.id.content, candidateDashboardFragment)
//                                        .commit();
//                            } else if (userRole.equals(UserRole.EMPLOYER)) {
//                                getSupportFragmentManager()
//                                        .beginTransaction()
//                                        .replace(R.id.content, employerDashboardFragment)
//                                        .commit();
//                            }
//                        } else {
//                            Log.d(LOG_TAG, "User role is null");
//                        }
//                        return true;
//                    case R.id.nav_search:
//                        getSupportFragmentManager().beginTransaction().replace(R.id.content, searchFragment).commit();
//                        return true;
//                    case R.id.nav_add_post:
//                        getSupportFragmentManager().beginTransaction().replace(R.id.content, notificationFragment).commit();
//                        return true;
//                    case R.id.nav_inbox:
//
//                        return true;
//                }
//                return false;
//            }
//        });

    }

    private void updateSelectedItem() {
        btnList.forEach(btn -> {
            if (btn.getId() == selectedButtonId) {
                System.out.println("SETTER");
                btn.setColorFilter(Color.parseColor("#0D0140"));
            } else {
                btn.setColorFilter(Color.parseColor("#A49EB5"));
            }
        });
    }
}