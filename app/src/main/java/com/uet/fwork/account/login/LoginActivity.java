package com.uet.fwork.account.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.uet.fwork.navbar.DashboardActivity;
import com.uet.fwork.R;
import com.uet.fwork.account.register.RegisterActivity;
import com.uet.fwork.account.resetpassword.ResetPasswordActivity;
import com.uet.fwork.chat.ChatActivity;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.repository.Repository;
import com.uet.fwork.database.repository.UserRepository;
import com.uet.fwork.firebasehelper.FirebaseAuthHelper;
import com.uet.fwork.firebasehelper.FirebaseSignInMethod;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {
    private final FirebaseAuth firebaseAuth;

    private TextInputLayout edtEmail, edtPassword;
    private Button btnLogin;
    private RelativeLayout btnLoginWithGoogle;
    private RelativeLayout btnLoginWithFacebook;
    //    private LoginButton btnLoginWithFacebook;
    private TextView txtCreateAccount;
    private TextView txtForgotPassword;

    private FirebaseAuthHelper firebaseAuthHelper;
    private UserRepository userRepository;

    private CallbackManager callbackManager;
    private LoginManager loginManager;

    private ActivityResultLauncher<Intent> getGoogleAccountActivityLauncher;

    public LoginActivity() {
        super();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuthHelper = new FirebaseAuthHelper(firebaseAuth);
//        FacebookSdk.sdkInitialize(getApplicationContext());
        userRepository = new UserRepository(FirebaseDatabase.getInstance());

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnLoginWithGoogle = findViewById(R.id.relLoginGoogle);
        btnLoginWithFacebook = findViewById(R.id.btnLoginFacebook);
        txtCreateAccount = findViewById(R.id.txtCreateAccount);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);

        initLoginWithGoogle();
        initLoginWithFacebook();

        btnLogin.setOnClickListener(btnLoginView -> loginWithEmailPassword());

        btnLoginWithGoogle.setOnClickListener(btnLoginWithGoogleView -> loginWithGoogle());

        btnLoginWithFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginManager.logInWithReadPermissions(LoginActivity.this, Arrays.asList("email", "public_profile"));
            }
        });

        txtCreateAccount.setOnClickListener(txtCreateAccountView -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        txtForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginFirebaseWithCredential(AuthCredential authCredential) {
        firebaseAuth.signInWithCredential(authCredential)
                .addOnSuccessListener(authResult -> {
                    userRepository.getUserByUID(firebaseAuth.getUid(), new Repository.OnQuerySuccessListener<UserModel>() {
                        @Override
                        public void onSuccess(UserModel result) {
                            //  Người dùng chưa khai báo thông tin cá nhân
                            if (result.getLastUpdate() == 0) {
                                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                                intent.putExtra("startDestinationId", R.id.selectUserRoleFragment);
                                startActivity(intent);
                            } else {
                                startDashboardActivity();
                            }
                        }
                    });
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    private void loginWithEmailPassword() {
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
                    .addOnFailureListener(exception -> edtEmail.setError("Email or password is invalid."))
                    .addOnSuccessListener(authResult -> {
                        //  Kiểm tra email người dùng đã được xác mình chưa
                        if (!firebaseAuth.getCurrentUser().isEmailVerified()) {
                            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                            intent.putExtra("startDestinationId", R.id.registerVerifyRequestFragment);
                            startActivity(intent);
                        }
                        //  Kiểm tra người dùng đã khai báo thông tin cá nhân chưa
                        else {
                            userRepository.getUserByUID(firebaseAuth.getUid(), new Repository.OnQuerySuccessListener<UserModel>() {
                                @Override
                                public void onSuccess(UserModel result) {
                                    //  Người dùng chưa khai báo thông tin cá nhân
                                    if (result.getLastUpdate() == 0) {
                                        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                                        intent.putExtra("startDestinationId", R.id.selectUserRoleFragment);
                                        startActivity(intent);
                                    } else {
                                        startDashboardActivity();
                                    }
                                }
                            });
                        }
                    });
        }
    }

    private void startDashboardActivity() {
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    }

    private void onFirebaseLoginSuccess() {
        if (!firebaseAuth.getCurrentUser().isEmailVerified()) {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            intent.putExtra("startDestinationId", R.id.registerVerifyRequestFragment);
            startActivity(intent);
        } else {
            userRepository.getUserByUID(firebaseAuth.getUid(), new Repository.OnQuerySuccessListener<UserModel>() {
                @Override
                public void onSuccess(UserModel result) {
                    //  Người dùng chưa khai báo thông tin cá nhân
                    if (result.getLastUpdate() == 0) {
                        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                        intent.putExtra("startDestinationId", R.id.selectUserRoleFragment);
                        startActivity(intent);
                    } else {
                        startDashboardActivity();
                    }
                }
            });
        }
    }

    private void initLoginWithGoogle() {
        getGoogleAccountActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                activityResult -> {
                    if (activityResult.getResultCode() == Activity.RESULT_OK) {
                        Task<GoogleSignInAccount> signInAccountTask =
                                GoogleSignIn.getSignedInAccountFromIntent(activityResult.getData());

                        signInAccountTask.addOnSuccessListener(googleSignInAccount -> {
                            if (googleSignInAccount != null) {
                                String email = googleSignInAccount.getEmail();
                                firebaseAuthHelper.isUserWithEmailAndSignInMethodExists(
                                        email, FirebaseSignInMethod.GOOGLE,
                                        result -> {
                                            if (result) {
                                                String googleIdToken = googleSignInAccount.getIdToken();
                                                AuthCredential authCredential =
                                                        GoogleAuthProvider.getCredential(googleIdToken, null);
                                                loginFirebaseWithCredential(authCredential);
                                            } else {
                                                //  UI thông báo lỗi
                                                Toast.makeText(LoginActivity.this,
                                                        "Tài khoản này không tồn tại", Toast.LENGTH_SHORT)
                                                        .show();
                                            }
                                        }
                                );
                            }
                        });
                    } else {
                        System.out.println("RESULT CODE " + activityResult.getResultCode());
                    }
                });
    }

    private void loginWithGoogle() {
        String oAuthToken = getString(R.string.default_web_client_id);
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(oAuthToken)
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
        Intent intent = googleSignInClient.getSignInIntent();
        getGoogleAccountActivityLauncher.launch(intent);
        googleSignInClient.signOut();
    }

    private void initLoginWithFacebook() {
        loginManager = LoginManager.getInstance();
        this.callbackManager = CallbackManager.Factory.create();
        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("FACEBOOK", "Callback success");
                firebaseAuthHelper.isFacebookUserExistsInFirebase(loginResult,
                        result -> {
                            if (result) {
                                AuthCredential credential =
                                        FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
                                loginFirebaseWithCredential(credential);
                            } else  {
                                //  UI thông báo tài khoản không tồn tại
                                Toast.makeText(LoginActivity.this,
                                                "Tài khoản này không tồn tại", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
            }

            @Override
            public void onCancel() {
                Log.d("FACEBOOK", "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                error.printStackTrace();
                Log.d("FACEBOOK", "facebook:error");
            }
        });
    }



    /**
     * Xử lý login bằng Facebook
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("FACEBOOK", "activity result");
        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}