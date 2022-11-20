package com.uet.fwork.account.resetpassword;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.uet.fwork.LoadingScreenDialog;
import com.uet.fwork.R;
import com.uet.fwork.firebasehelper.FirebaseAuthHelper;
import com.uet.fwork.firebasehelper.FirebaseSignInMethod;

import java.util.List;

public class ResetPasswordEnterEmailFragment extends Fragment {

    private TextInputLayout edtEmail;
    private ImageButton btnSubmit;
    private FirebaseAuthHelper authHelper;

    public ResetPasswordEnterEmailFragment() {
        super(R.layout.fragment_reset_password_enter_email);
        authHelper = new FirebaseAuthHelper(FirebaseAuth.getInstance());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        edtEmail = view.findViewById(R.id.tilEmail);
        btnSubmit = view.findViewById(R.id.btnNext);
        btnSubmit.setOnClickListener(button -> {
            edtEmail.setErrorEnabled(false);
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            String emailAddress = edtEmail.getEditText().getText().toString();

            authHelper.getUserSignInMethod(emailAddress, signInMethods -> {
                if (signInMethods == null) {
                    edtEmail.setError("Email không hợp lệ");
                }
                else if (!signInMethods.contains(FirebaseSignInMethod.PASSWORD)) {
                    edtEmail.setError("Email này không được đăng nhập bằng mật khẩu");
                } else {
                    LoadingScreenDialog dialog = new LoadingScreenDialog(getContext());
                    dialog.show();

                    firebaseAuth.sendPasswordResetEmail(emailAddress).addOnSuccessListener(unused -> {
                        dialog.dismiss();
                        Navigation.findNavController(getActivity(),
                                        R.id.navigation_host)
                                .navigate(R.id.action_resetPasswordEnterEmailFragment_to_resetPasswordFragment);
                    }).addOnFailureListener(e -> e.printStackTrace());
                }
            });
        });
    }
}
