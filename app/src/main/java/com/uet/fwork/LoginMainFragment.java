package com.uet.fwork;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.model.UserRole;
import com.uet.fwork.database.repository.Repository;
import com.uet.fwork.database.repository.UserRepository;

public class LoginMainFragment extends Fragment {

    private FirebaseAuth firebaseAuth;

    private TextInputLayout edtEmail, edtPassword;
    private Button btnLogin;
    private RelativeLayout btnLoginWithGoogle;
    private TextView txtCreateAccount;

    private GoogleSignInClient googleSignInClient;
    private UserRepository userRepository;

    public LoginMainFragment() {
        super(R.layout.fragment_login_main);
        firebaseAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository(FirebaseDatabase.getInstance());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtEmail = (TextInputLayout) view.findViewById(R.id.edtEmail);
        edtPassword = (TextInputLayout) view.findViewById(R.id.edtPassword);
        btnLogin = (Button) view.findViewById(R.id.btnLogin);
        btnLoginWithGoogle = (RelativeLayout) view.findViewById(R.id.relLoginGoogle);
        txtCreateAccount = (TextView) view.findViewById(R.id.txtCreateAccount);

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
                                    Intent intent = new Intent(getContext(), HelloActivity.class);
                                    startActivity(intent);
                                }
                            });
                }
            }
        });

        txtCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RegisterActivity.class);
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
                    if (result.getResultCode() == Activity.RESULT_OK) {
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
                                                        userRepository.getUserByUID(firebaseAuth.getCurrentUser().getUid(),
                                                                new Repository.OnQuerySuccessListener<UserModel>() {
                                                                    @Override
                                                                    public void onSuccess(UserModel result) {
                                                                        if (result == null) {
                                                                            createUserData();
                                                                            Navigation.findNavController(getActivity(), R.id.navigation_host)
                                                                                    .navigate(
                                                                                            R.id.action_loginMainFragment_to_selectUserRoleFragment
                                                                                    );
                                                                        }
                                                                    }
                                                                });
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

    private UserModel createUserData() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        UserModel userModel = new UserModel(
                firebaseUser.getUid(), firebaseUser.getEmail(), "", "", "", "", ""
        );
        userRepository.insertUser(userModel);
        return userModel;
    }

    private void handleLoginWithGoogle() {
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(getContext(), googleSignInOptions);
        btnLoginWithGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = googleSignInClient.getSignInIntent();
                googleLoginActivityResultLauncher.launch(intent);
            }
        });
    }

}
