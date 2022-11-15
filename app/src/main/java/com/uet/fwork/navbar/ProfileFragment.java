package com.uet.fwork.navbar;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.uet.fwork.R;
import com.uet.fwork.UpdateProfileActivity;
import com.uet.fwork.account.ChangePasswordActivity;
import com.uet.fwork.account.login.LoginActivity;
import com.uet.fwork.firebasehelper.FirebaseAuthHelper;
import com.uet.fwork.firebasehelper.FirebaseSignInMethod;

import java.util.List;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseAuthHelper firebaseAuthHelper;

    private ImageView avatarIv;
    private TextView nameTv, emailTv, phoneTv, sexTv, birthTv, workYearTv, jobTv;

    private TextView txtChangePassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuthHelper = new FirebaseAuthHelper(firebaseAuth);
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("users");

        txtChangePassword = (TextView) view.findViewById(R.id.txtChangePassword);
        txtChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ChangePasswordActivity.class);
                startActivity(intent);
            }
        });
        firebaseAuthHelper.getUserSignInMethod(firebaseAuth.getCurrentUser().getEmail(), new FirebaseAuthHelper.OnSuccessListener<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                if (result.contains(FirebaseSignInMethod.PASSWORD)) {
                    txtChangePassword.setVisibility(View.VISIBLE);
                }
            }
        });

        Button btnLogout = (Button) view.findViewById(R.id.return_button);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });

        TextView txtEditProfile = (TextView) view.findViewById(R.id.edtProfile_button);
        txtEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), UpdateProfileActivity.class);
                startActivity(intent);
            }
        });

        avatarIv = view.findViewById(R.id.avatarIv);
        nameTv = view.findViewById(R.id.nameTv);
        emailTv = view.findViewById(R.id.emailTv);
        phoneTv = view.findViewById(R.id.phoneTv);
        sexTv = view.findViewById(R.id.sexTv);
        jobTv = view.findViewById(R.id.jobTv);
        workYearTv = view.findViewById(R.id.workYearTv);
        birthTv = view.findViewById(R.id.birthTv);


        // Đoạn này m có thể dùng userRepository để lấy ra user bằng udi thì hơn ấy
        //  Hoặc m có thể querry trực tiếp như thế này databaseReference.child(uid).get
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String name = "" + ds.child("fullName").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phoneNumber = "" + ds.child("phoneNumber").getValue();
                    String image = "" + ds.child("avatar").getValue();
                    String job = "" + ds.child("major").getValue();
                    String workYears = "" + ds.child("yearOfExperience").getValue() + " năm";
                    String sex = "" + ds.child("sex").getValue();
                    String dateOfBirth = "" + ds.child("dateOfBirth").getValue();
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phoneNumber);
                    jobTv.setText(job);
                    workYearTv.setText(workYears);
                    sexTv.setText(sex);
                    birthTv.setText(dateOfBirth);


                    if (image != null && !image.equals("")) {
                        Picasso.get().load(image).placeholder(R.drawable.wlop_33se).into(avatarIv);
                    }
                    //Picasso.get().load(image).into(avatarIv);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }

    @Override
    public void onClick(View view) {

    }
}