package com.uet.fwork;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class RegisterVerifyRequestFragment extends Fragment {
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private CountDownTimer countDownTimer;

    private boolean canSendVerificationEmail = false;
    private final long RE_SEND_EMAIL_TIME = 30000;

    private Button btnResendEmail;

    public RegisterVerifyRequestFragment() {
        super(R.layout.fragment_register_verify_request);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button btnEmailVerified = view.findViewById(R.id.emailVerified);
        btnResendEmail = view.findViewById(R.id.btnResendEmail);

        emailVerificationCountdown().start();

        btnResendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!canSendVerificationEmail) {
                    Toast.makeText(getActivity(), "Cannot send", Toast.LENGTH_SHORT).show();
                    return;
                }

                emailVerificationCountdown().start();
            }
        });

        btnEmailVerified.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user == null) {
                    user = firebaseAuth.getCurrentUser();
                } else {
                    user.reload();
                }

                if (user == null) {
                    return;
                }

                if (user.isEmailVerified()) {
                    System.out.println(user.getEmail());
                    Navigation.findNavController(getActivity(), R.id.navigation_host)
                            .navigate(R.id.action_registerVerifyRequestFragment_to_registerCreateProfileFragment);
                } else {
                    Toast.makeText(getActivity(), "Email is not verified", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private CountDownTimer emailVerificationCountdown() {
        canSendVerificationEmail = false;
        btnResendEmail.setEnabled(false);
//        btnResendEmail.setBackgr
        return new CountDownTimer(RE_SEND_EMAIL_TIME, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                long currentTime = RE_SEND_EMAIL_TIME - millisUntilFinished;
                btnResendEmail.setText("Resend Email (" + (millisUntilFinished / 1000) + "s)");
            }

            @Override
            public void onFinish() {
                btnResendEmail.setText("Resend Email");
                canSendVerificationEmail = true;
                btnResendEmail.setEnabled(true);
            }
        };
    }
}