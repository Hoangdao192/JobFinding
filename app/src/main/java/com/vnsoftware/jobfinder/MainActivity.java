package com.vnsoftware.jobfinder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.FirebaseDatabase;
import com.vnsoftware.jobfinder.account.login.LoginActivity;
import com.vnsoftware.jobfinder.account.register.RegisterActivity;
import com.vnsoftware.jobfinder.database.model.UserModel;
import com.vnsoftware.jobfinder.database.repository.Repository;
import com.vnsoftware.jobfinder.database.repository.UserRepository;
import com.vnsoftware.jobfinder.firebasehelper.CloudMessagingHelper;
import com.vnsoftware.jobfinder.firebasehelper.FirebaseAuthHelper;
import com.vnsoftware.jobfinder.firebasehelper.FirebaseSignInMethod;
import com.vnsoftware.jobfinder.landingpage.LandingPage1;
import com.vnsoftware.jobfinder.landingpage.FirstLaunchActivity;
import com.vnsoftware.jobfinder.navbar.DashboardActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private UserRepository userRepository;

    public MainActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CloudMessagingHelper.initialize(this);
        Repository.initialize(this, FirebaseDatabase.getInstance());
        FirebaseAuthHelper.initialize(
                FirebaseDatabase.getInstance(), FirebaseAuth.getInstance(),
                getApplicationContext());

        this.firebaseAuth = FirebaseAuth.getInstance();
        this.userRepository = UserRepository.getInstance();

//        startActivity(new Intent(this, LoginActivity.class));

//          Truy cập lần đầu
        SharedPreferences sharedPreferences = this.getSharedPreferences("MAIN", MODE_PRIVATE);
        boolean firstLaunch = sharedPreferences.getBoolean("FIRST_LAUNCH", true);
        String currentUserId = sharedPreferences.getString("USER", "");
        if (firstLaunch) {
            sharedPreferences.edit().putBoolean("FIRST_LAUNCH", false).apply();
            startActivity(new Intent(this, FirstLaunchActivity.class));
        } else {
            if (isNetworkAvailable()) {
                listenNetworkState();
                initNotificationChanel();
                checking();
            } else {
                Intent intent = new Intent(MainActivity.this, InternetErrorActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
    }

    private void checking() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences sharedPreferences = this.getSharedPreferences("MAIN", MODE_PRIVATE);
        String currentUserId = sharedPreferences.getString("USER", "");
        System.out.println(currentUserId);
        if (firebaseUser != null) {
            firebaseUser.reload().addOnSuccessListener(
                            new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
//                                    if (!currentUserId.equals("")) {
//                                        System.out.println(currentUserId);
//                                        startDashboardActivity();
//                                    }
                                    FirebaseAuth.getInstance().fetchSignInMethodsForEmail(firebaseUser.getEmail())
                                            .addOnSuccessListener(new OnSuccessListener<SignInMethodQueryResult>() {
                                                @Override
                                                public void onSuccess(SignInMethodQueryResult signInMethodQueryResult) {
                                                    List<String> methods = signInMethodQueryResult.getSignInMethods();
                                                    if (methods.contains(FirebaseSignInMethod.GOOGLE)
                                                            || methods.contains(FirebaseSignInMethod.FACEBOOK)) {
                                                        userRepository.getUserByUID(firebaseAuth.getUid(), new Repository.OnQuerySuccessListener<UserModel>() {
                                                            @Override
                                                            public void onSuccess(UserModel result) {
                                                                //  Người dùng chưa khai báo thông tin cá nhân
                                                                if (result.getLastUpdate() == 0) {
                                                                    Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                                                                    intent.putExtra("startDestinationId", R.id.selectUserRoleFragment);
                                                                    startActivity(intent);
                                                                } else {
                                                                    startDashboardActivity();
                                                                }
                                                            }
                                                        });
                                                    }
                                                    else {
                                                        //  Kiểm tra email người dùng đã được xác mình chưa
                                                        if (!firebaseAuth.getCurrentUser().isEmailVerified()) {
                                                            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                                                            intent.putExtra("startDestinationId", R.id.registerVerifyRequestFragment);
                                                            startActivity(intent);
                                                            System.out.println("CHECKING EMAIL");
                                                        }
                                                        //  Kiểm tra người dùng đã khai báo thông tin cá nhân chưa
                                                        else {
                                                            userRepository.getUserByUID(firebaseAuth.getUid(), new Repository.OnQuerySuccessListener<UserModel>() {
                                                                @Override
                                                                public void onSuccess(UserModel result) {
                                                                    //  Người dùng chưa khai báo thông tin cá nhân
                                                                    if (result.getLastUpdate() == 0) {
                                                                        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                                                                        intent.putExtra("startDestinationId", R.id.selectUserRoleFragment);
                                                                        startActivity(intent);
                                                                    } else {
                                                                        startDashboardActivity();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                }
                                            });
//                                    startDashboardActivity();
                                }
                            }
                    )
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                            if (e instanceof FirebaseAuthInvalidUserException) {
                                startLoginActivity();
                            }
                        }
                    });
        } else {
            startLoginActivity();
//            Intent intent = new Intent(this, LandingPage1.class);
//            startActivity(intent);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = getSystemService(ConnectivityManager.class);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void listenNetworkState() {
        ConnectivityManager connectivityManager = getSystemService(ConnectivityManager.class);
        connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
            }

            @Override
            public void onLost(Network network) {
                System.out.println("LOST CONNECTION");
                Intent intent = new Intent(MainActivity.this, InternetErrorActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private void startDashboardActivity() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("MAIN", MODE_PRIVATE);
        sharedPreferences.edit().putString("USER", firebaseAuth.getUid()).apply();
        FirebaseAuthHelper.getInstance().fetchCurrentUserData(isSuccess -> {
            if (isSuccess) {
                Intent intent = new Intent(this, DashboardActivity.class);
                startActivity(intent);
            }
        });
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void startFirstLaunch() {
        Intent intent = new Intent(this, LandingPage1.class);
        startActivity(intent);
    }

    private void initNotificationChanel() {
        CharSequence name = "Tin nhắn";
        String description = "Thông báo khi có tin nhắn gửi đến";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        String CHANNEL_ID = "Message";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}