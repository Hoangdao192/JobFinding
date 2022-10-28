package com.uet.fwork.user;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.uet.fwork.R;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseUser user;
    private DatabaseReference reference;

    private String userID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        user = FirebaseAuth.getInstance().getCurrentUser();
        //reference = FirebaseDatabase.getInstance().getReference("users");
        reference = FirebaseDatabase.getInstance().getReference("users").child(userID);
        userID = user.getUid();

        final TextView fullName = (TextView) findViewById(R.id.fullName);
        final TextView email = (TextView) findViewById(R.id.email);
        final TextView phoneNumber = (TextView) findViewById(R.id.phoneNumber);
        final TextView major = (TextView) findViewById(R.id.major);


        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);
                if (userProfile != null) {
                    fullName.setText(userProfile.getFullName());
                    email.setText(userProfile.getEmail());
                    phoneNumber.setText(userProfile.getPhoneNumber());
                    major.setText(userProfile.getMajor());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
            }
        });
    }

}

