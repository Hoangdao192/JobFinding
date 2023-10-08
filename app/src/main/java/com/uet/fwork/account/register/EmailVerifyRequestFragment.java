package com.uet.fwork.account.register;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.uet.fwork.R;


public class EmailVerifyRequestFragment extends Fragment {
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

//    private boolean canSendVerificationEmail = false;
    private final long RE_SEND_EMAIL_TIME = 30000;

    private NavController navController;
    private Button btnResendEmail;

    public EmailVerifyRequestFragment() {
        super(R.layout.fragment_register_verify_request);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnEmailVerified = view.findViewById(R.id.emailVerified);
        btnResendEmail = view.findViewById(R.id.btnResendEmail);
        navController = Navigation.findNavController(getActivity(), R.id.navigation_host);

        emailVerificationCountdown().start();

        btnResendEmail.setOnClickListener(v -> {
            firebaseAuth.getCurrentUser()
                    .sendEmailVerification()
                    .addOnSuccessListener(unused -> Toast.makeText(
                            getActivity(), "Đã gửi lại email xác thực", Toast.LENGTH_SHORT)
                            .show());
            emailVerificationCountdown().start();
        });

        btnEmailVerified.setOnClickListener(v -> {
            if (user == null) {
                user = firebaseAuth.getCurrentUser();
            }

            //  Reload lại user từ Firebase server
            user.reload();

            if (user.isEmailVerified()) {
                navController.navigate(R.id.action_registerVerifyRequestFragment_to_selectUserRoleFragment);
            } else {
                Toast.makeText(getActivity(), "Email is not verified", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private CountDownTimer emailVerificationCountdown() {
        btnResendEmail.setEnabled(false);

        return new CountDownTimer(RE_SEND_EMAIL_TIME, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                btnResendEmail.setText("Resend Email (" + (millisUntilFinished / 1000) + "s)");
            }

            @Override
            public void onFinish() {
                btnResendEmail.setText("Resend Email");
                btnResendEmail.setEnabled(true);
            }
        };
    }
}