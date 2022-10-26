package com.uet.fwork;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private TextInputLayout edtEmail, edtPassword;
    private Button btnLogin;
    private TextView txtCreateAccount;

    public LoginActivity() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = (TextInputLayout) findViewById(R.id.edtEmail);
        edtPassword = (TextInputLayout) findViewById(R.id.edtPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        txtCreateAccount = (TextView) findViewById(R.id.txtCreateAccount);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtEmail.setErrorEnabled(false);
                edtPassword.setErrorEnabled(false);

                String email = edtEmail.getEditText().getText().toString();
                String password = edtPassword.getEditText().getText().toString();
                if (email.length() == 0) {
                    edtEmail.setError("Email is empty");
                } else if (password.length() == 0) {
                    edtPassword.setError("Password is empty");
                } else {
                    firebaseAuth.signInWithEmailAndPassword(email, password)
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    edtEmail.setError("Email or password is invalid.");
                                }
                            })
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                }
                            });
                }
            }
        });

        txtCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}