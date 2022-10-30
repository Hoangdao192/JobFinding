package com.uet.fwork;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.uet.fwork.account.login.LoginActivity;
import com.uet.fwork.util.VietNameAdministrativeDivisionAPI;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {
            System.out.println(FirebaseAuth.getInstance().getUid());
            System.out.println(FirebaseAuth.getInstance().getCurrentUser().getUid());
            startActivity(new Intent(this, HelloActivity.class));
        }

//        VietNameAdministrativeDivisionAPI api = VietNameAdministrativeDivisionAPI.getInstance();
//        api.getProvinceList(this, result -> {
//
//        });
    }
}