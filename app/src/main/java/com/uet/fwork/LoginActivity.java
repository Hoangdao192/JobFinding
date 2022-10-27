package com.uet.fwork;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private TextInputLayout edtEmail, edtPassword;
    private Button btnLogin;
    private RelativeLayout btnLoginWithGoogle;
    private TextView txtCreateAccount;

    private GoogleSignInClient googleSignInClient;

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
        btnLoginWithGoogle = (RelativeLayout) findViewById(R.id.relLoginGoogle);
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
                                    Intent intent = new Intent(LoginActivity.this, HelloActivity.class);
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

        handleLoginWithGoogle();
    }

    ActivityResultLauncher<Intent> googleLoginActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Task<GoogleSignInAccount> signInAccountTask = GoogleSignIn
                                .getSignedInAccountFromIntent(result.getData());

                        if(signInAccountTask.isSuccessful()) {
                            try {
                                GoogleSignInAccount googleSignInAccount = signInAccountTask
                                        .getResult(ApiException.class);
                                // Check condition
                                if (googleSignInAccount != null) {
                                    AuthCredential authCredential = GoogleAuthProvider
                                            .getCredential(googleSignInAccount.getIdToken(), null);
                                    firebaseAuth.signInWithCredential(authCredential)
                                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if (task.isSuccessful()) {
                                                        System.out.println(firebaseAuth.getCurrentUser().getEmail());
                                                    } else {
                                                        task.getException().printStackTrace();
                                                    }
                                                }
                                            });

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        System.out.println("RESULT CODE " + result.getResultCode());
                    }
                }
            });

    private void handleLoginWithGoogle() {
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
        btnLoginWithGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = googleSignInClient.getSignInIntent();
                googleLoginActivityResultLauncher.launch(intent);
            }
        });
    }

}