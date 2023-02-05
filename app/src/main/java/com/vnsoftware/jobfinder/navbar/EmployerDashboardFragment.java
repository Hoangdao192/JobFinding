package com.vnsoftware.jobfinder.navbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;
import com.vnsoftware.jobfinder.R;
import com.vnsoftware.jobfinder.account.ChangePasswordActivity;
import com.vnsoftware.jobfinder.account.login.LoginActivity;
import com.vnsoftware.jobfinder.account.profile.ProfileFragment;
import com.vnsoftware.jobfinder.database.model.UserModel;
import com.vnsoftware.jobfinder.database.model.UserRole;
import com.vnsoftware.jobfinder.firebasehelper.FirebaseAuthHelper;
import com.vnsoftware.jobfinder.firebasehelper.FirebaseSignInMethod;
import com.vnsoftware.jobfinder.post.EmployerShowPostApplyFragment;
import com.vnsoftware.jobfinder.post.ShowMyPostFragment;
import com.vnsoftware.jobfinder.post.ShowPostLikeFragment;

import de.hdodenhof.circleimageview.CircleImageView;

public class EmployerDashboardFragment extends Fragment {

    private CircleImageView cirImgAvatar;
    private TextView txvFullName, txvEmail;
    private LinearLayout lltProfile, lltPost, lltInterest;
    private LinearLayout lltPostApply, lltChangePassword, lltSignOut;
    private ProfileFragment profileFragment;

    private FirebaseAuthHelper firebaseAuthHelper;

    public EmployerDashboardFragment() {
        super(R.layout.fragment_employer_dashboard);
        firebaseAuthHelper = FirebaseAuthHelper.getInstance();

        profileFragment = new ProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putString("USER_ROLE", UserRole.EMPLOYER);
        profileFragment.setArguments(bundle);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cirImgAvatar = view.findViewById(R.id.imgAvatar);
        txvFullName = view.findViewById(R.id.txtFullName);
        txvEmail = view.findViewById(R.id.txtEmail);
        lltProfile = view.findViewById(R.id.relProfile);
        lltPost = view.findViewById(R.id.relPost);
        lltInterest = view.findViewById(R.id.relInterestPost);
        lltPostApply = view.findViewById(R.id.relApplyPost);
        lltChangePassword = view.findViewById(R.id.relChangePassword);
        lltSignOut = view.findViewById(R.id.relSignOut);

        String signInMethod = firebaseAuthHelper.getSignInMethod();
        if (!signInMethod.equals(FirebaseSignInMethod.PASSWORD)) {
            lltChangePassword.setVisibility(View.GONE);
        }
        UserModel userModel = firebaseAuthHelper.getUser();
        if (!userModel.getAvatar().equals("")) {
            Picasso.get().load(userModel.getAvatar()).into(cirImgAvatar);
        }
        txvFullName.setText(userModel.getFullName());
        txvEmail.setText(userModel.getEmail());

        lltProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack("ProfileFragment")
                        .replace(R.id.content, profileFragment)
                        .commit();
            }
        });

        lltPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack("PostListFragment")
                        .replace(R.id.content, new ShowMyPostFragment())
                        .commit();
            }
        });

        lltInterest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack("PostLikeFragment")
                        .replace(R.id.content, new ShowPostLikeFragment())
                        .commit();
            }
        });

        lltPostApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack("ShowPostApply")
                        .replace(R.id.content, new EmployerShowPostApplyFragment())
                        .commit();
            }
        });

        lltChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
                startActivity(intent);
            }
        });

        lltSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuthHelper.signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });
    }
}