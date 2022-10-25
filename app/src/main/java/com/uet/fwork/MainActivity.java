package com.uet.fwork;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.repository.Repository;
import com.uet.fwork.database.repository.UserRepository;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UserRepository userRepository = new UserRepository(FirebaseDatabase.getInstance());
        userRepository.getUserByUID("NkepwVHwnRhx4VUdBUZEGKPUgRJ3",
                new Repository.OnQuerySuccessListener<UserModel>() {
            @Override
            public void onSuccess(UserModel result) {

            }
        });
    }
}