package com.uet.fwork.account;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.uet.fwork.HelloActivity;
import com.uet.fwork.LoadingScreenDialog;
import com.uet.fwork.R;

import java.util.Objects;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputLayout edtPassword, edtRePassword;
    private Button btnChangePassword;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        firebaseAuth = FirebaseAuth.getInstance();

        edtPassword = (TextInputLayout) findViewById(R.id.edtPassword);
        edtRePassword = (TextInputLayout) findViewById(R.id.edtRePassword);
        btnChangePassword = (Button) findViewById(R.id.btnChangePassword);
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser() != null && checkUserInput()) {
                    LoadingScreenDialog dialog = new LoadingScreenDialog(ChangePasswordActivity.this);
                    dialog.show();
                    firebaseAuth.getCurrentUser().updatePassword(edtPassword.getEditText().getText().toString())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Intent intent = new Intent(ChangePasswordActivity.this, HelloActivity.class);
                                    startActivity(intent);
                                    dialog.dismiss();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    e.printStackTrace();
                                }
                            });
                }
            }
        });
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