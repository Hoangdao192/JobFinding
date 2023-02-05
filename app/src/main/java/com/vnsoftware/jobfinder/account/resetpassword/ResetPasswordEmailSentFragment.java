package com.vnsoftware.jobfinder.account.resetpassword;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.vnsoftware.jobfinder.R;
import com.vnsoftware.jobfinder.account.login.LoginActivity;

public class ResetPasswordEmailSentFragment extends Fragment {

    private ImageButton btnLogin;

    public ResetPasswordEmailSentFragment() {
        super(R.layout.fragment_reset_password_email_sent);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnLogin = view.findViewById(R.id.btnNext);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                getActivity().startActivity(intent);
            }
        });
    }
}
