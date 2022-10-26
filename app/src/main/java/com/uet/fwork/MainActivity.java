package com.uet.fwork;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;
import com.uet.fwork.database.model.CandidateModel;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.repository.Repository;
import com.uet.fwork.database.repository.UserRepository;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CandidateModel candidateModel = new CandidateModel(
                "1",
                "", "", "", "", "", "", "25/10/2022"
        );
        FirebaseDatabase.getInstance().getReference("users/")
                .child("1").setValue(candidateModel);
    }
}