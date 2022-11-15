package com.uet.fwork.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.uet.fwork.LoadingScreenDialog;
import com.uet.fwork.R;
import com.uet.fwork.firebasehelper.FirebaseAuthHelper;
import com.uet.fwork.navbar.DashboardActivity;

public class ChangePasswordActivity extends AppCompatActivity {

    private FirebaseAuthHelper firebaseAuthHelper;
    private TextInputLayout edtOldPassword, edtPassword, edtRePassword;
    private ImageButton btnChangePassword;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuthHelper = new FirebaseAuthHelper(firebaseAuth);

        edtOldPassword = (TextInputLayout) findViewById(R.id.edtOldPassword);
        edtPassword = findViewById(R.id.edtPassword);
        edtRePassword = findViewById(R.id.edtRePassword);
        btnChangePassword = (ImageButton) findViewById(R.id.imgBtn);
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTextInputLayoutError();
                if (firebaseAuth.getCurrentUser() != null && checkUserInput()) {
                    LoadingScreenDialog dialog = new LoadingScreenDialog(ChangePasswordActivity.this);
                    dialog.show();

                    String currentPassword = edtOldPassword.getEditText().getText().toString();
                    //  Đăng nhập lại người dùng
                    firebaseAuth.signInWithEmailAndPassword(
                            firebaseAuth.getCurrentUser().getEmail(),
                            currentPassword
                    )
                            .addOnFailureListener(exception -> {
                                edtOldPassword.setError("Sai mật khẩu");
                                dialog.dismiss();
                            })
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    firebaseAuth.getCurrentUser().updatePassword(edtPassword.getEditText().getText().toString())
                                            .addOnSuccessListener(unused -> {
                                                Intent intent = new Intent(ChangePasswordActivity.this, DashboardActivity.class);
                                                startActivity(intent);
                                                dialog.dismiss();
                                            })
                                            .addOnFailureListener(Throwable::printStackTrace);
                                }
                            });
                }
            }
        });
    }

    private void resetTextInputLayoutError() {
        edtOldPassword.setErrorEnabled(false);
        edtPassword.setErrorEnabled(false);
        edtRePassword.setErrorEnabled(false);
    }

    private boolean checkUserInput() {
        String password = edtPassword.getEditText().getText().toString();
        String rePassword = edtRePassword.getEditText().getText().toString();

        if (password.length() == 0) {
            edtPassword.setError("Enter your password.");
            edtPassword.requestFocus();
            return false;
        }

        if (rePassword.length() == 0) {
            edtRePassword.setError("Confirm your password.");
            edtRePassword.requestFocus();
            return false;
        }

        if (!password.equals(rePassword)) {
            edtRePassword.setError("Password does not match.");
            edtRePassword.requestFocus();
            return false;
        }
        return true;
    }
}