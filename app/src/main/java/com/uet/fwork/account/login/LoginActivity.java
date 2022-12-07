package com.uet.fwork.account.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

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
import com.google.firebase.messaging.FirebaseMessaging;
import com.uet.fwork.dialog.ErrorDialog;
import com.uet.fwork.dialog.LoadingScreenDialog;
import com.uet.fwork.R;
import com.uet.fwork.account.register.RegisterActivity;
import com.uet.fwork.account.resetpassword.ResetPasswordActivity;
import com.uet.fwork.database.model.UserDeviceModel;
import com.uet.fwork.database.repository.UserDeviceRepository;
import com.uet.fwork.database.repository.UserRepository;
import com.uet.fwork.firebasehelper.FirebaseAuthHelper;
import com.uet.fwork.firebasehelper.FirebaseSignInMethod;
import com.uet.fwork.navbar.DashboardActivity;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {
    private static final String LOG_TAG = "Login activity";

    private TextInputLayout edtEmail, edtPassword;
    private Button btnLogin;
    private Button btnLoginWithGoogle;
    private Button btnLoginWithFacebook;
    private TextView txtCreateAccount;
    private TextView txtForgotPassword;
    private ImageButton imgBtnBack;

    private final FirebaseAuth firebaseAuth;
    private final FirebaseAuthHelper firebaseAuthHelper;
    private final UserRepository userRepository;
    private final UserDeviceRepository userDeviceRepository;

    //  Facebook login handle
    private CallbackManager callbackManager;
    private LoginManager loginManager;
    //  Google login handle
    private ActivityResultLauncher<Intent> getGoogleAccountActivityLauncher;

    public LoginActivity() {
        super();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuthHelper = FirebaseAuthHelper.getInstance();
        userRepository = UserRepository.getInstance();
        userDeviceRepository = UserDeviceRepository.getInstance();
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
        imgBtnBack = (ImageButton) findViewById(R.id.imgBtnBack);

        initLoginWithGoogle();
        initLoginWithFacebook();

        imgBtnBack.setOnClickListener(imageView -> finish());

        btnLogin.setOnClickListener(btnLoginView -> loginWithEmailPassword());

        btnLoginWithGoogle.setOnClickListener(btnLoginWithGoogleView -> loginWithGoogle());

        btnLoginWithFacebook.setOnClickListener(button ->
                loginManager.logInWithReadPermissions(
                        LoginActivity.this,
                        Arrays.asList("email", "public_profile")));

        txtCreateAccount.setOnClickListener(txtCreateAccountView -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        txtForgotPassword.setOnClickListener(textView -> {
            Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void loginFirebaseWithCredential(AuthCredential authCredential) {
        LoadingScreenDialog dialog = new LoadingScreenDialog(this);
        dialog.show();
        Log.d("LOGIN", "Firebase auth: Login with credential " + authCredential.toString());
        firebaseAuth.signInWithCredential(authCredential)
                .addOnSuccessListener(authResult -> {
                    userRepository.getUserByUID(firebaseAuth.getUid(), userModel -> {
                        dialog.dismiss();
                        Log.d(LOG_TAG, "Checking user data " + userModel.getId());
                        //  Người dùng chưa khai báo thông tin cá nhân
                        if (userModel.getLastUpdate() == 0) {
                            Log.d("LOGIN", "User data not exists" + userModel.getId());
                            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                            intent.putExtra("startDestinationId", R.id.selectUserRoleFragment);
                            startActivity(intent);
                        } else {
                            Log.d("LOGIN", "Checking ok" + userModel.getId());
                            onLoginSuccessful();
                        }
                    });
                })
                .addOnFailureListener(exception -> {
                    exception.printStackTrace();
                    Log.d(LOG_TAG, "Sign in with credential failed " + authCredential.toString());
                });
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
            LoadingScreenDialog dialog = new LoadingScreenDialog(this);
            dialog.show();
            Log.d(LOG_TAG, "Firebase auth: Login with password " + email + " " + password);
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnFailureListener(exception -> {
                        exception.printStackTrace();
                        edtEmail.setError("Email hoặc mật khẩu không hợp lệ.");
                        dialog.dismiss();
                        Log.d(LOG_TAG, "Firebase auth: Email or password not valid " + email);
                    })
                    .addOnSuccessListener(authResult -> {
                        dialog.dismiss();
                        Log.d(LOG_TAG, "Firebase auth: Email not valid " + email);
                        onFirebaseLoginSuccess();
                    });
        }
    }

    /**
     * Kiểm tra và đăng kí token thiết bị để nhận thông báo từ server
     */
    private void registerFirebaseMessaging() {
        Log.d(LOG_TAG, "Firebase messaging: Checking and register device token");
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    Log.d(LOG_TAG, "Firebase messaging: Check if user device exists");
                    userDeviceRepository.isUserDeviceExists(firebaseAuth.getUid(), exists -> {
                        UserDeviceModel userDevice= new UserDeviceModel(
                                firebaseAuth.getUid(),
                                token
                        );
                        if (!exists) {
                            Log.d(LOG_TAG, "Firebase messaging: User device not exists, insert new device");
                            userDeviceRepository.insert(userDevice);
                        } else {
                            Log.d(LOG_TAG, "Firebase messaging: User device exists, update device");
                            userDeviceRepository.update(userDevice);
                        }
                    });
                })
                .addOnFailureListener(exception -> {
                    exception.printStackTrace();
                    Log.e(LOG_TAG, "Firebase messaging: Failed to checking and register device token");
                })
                .addOnCanceledListener(() ->
                        Log.w(LOG_TAG, "Firebase messaging: Checking and register device token has been cancelled"));
    }

    private void onLoginSuccessful() {
        registerFirebaseMessaging();
        Log.d(LOG_TAG, "Firebase auth: Login successful");
        FirebaseAuthHelper.initialize(
                FirebaseDatabase.getInstance(), FirebaseAuth.getInstance(),
                getApplicationContext(), result -> {
                    Intent intent = new Intent(this, DashboardActivity.class);
                    startActivity(intent);
                });
    }

    private void onFirebaseLoginSuccess() {
        if (!firebaseAuth.getCurrentUser().isEmailVerified()) {
            Log.d(LOG_TAG, "Firebase auth: User email is not verified");
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            intent.putExtra("startDestinationId", R.id.registerVerifyRequestFragment);
            startActivity(intent);
        } else {
            userRepository.getUserByUID(firebaseAuth.getUid(), user -> {
                //  Người dùng chưa khai báo thông tin cá nhân
                if (user.getLastUpdate() == 0) {
                    Log.d(LOG_TAG, "Firebase auth: User data not exists");
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    intent.putExtra("startDestinationId", R.id.selectUserRoleFragment);
                    startActivity(intent);
                } else {
                    onLoginSuccessful();
                }
            });
        }
    }

    private void initLoginWithGoogle() {
        getGoogleAccountActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                activityResult -> {
                    if (activityResult.getResultCode() == Activity.RESULT_OK) {
                        Log.d(LOG_TAG, "Google: Google account pick launch ok");
                        LoadingScreenDialog dialog = new LoadingScreenDialog(LoginActivity.this);
                        dialog.show();

                        Log.d(LOG_TAG, "Google: Get sign in account task");
                        Task<GoogleSignInAccount> signInAccountTask =
                                GoogleSignIn.getSignedInAccountFromIntent(activityResult.getData());
                        signInAccountTask.addOnSuccessListener(googleSignInAccount -> {
                            Log.d(LOG_TAG, "Google: sign in task successful");
                            dialog.dismiss();
                            if (googleSignInAccount != null) {
                                String email = googleSignInAccount.getEmail();
                                Log.d(LOG_TAG, "Google: Account get successful " + email);
                                firebaseAuthHelper.isUserWithEmailAndSignInMethodExists(
                                        email, FirebaseSignInMethod.GOOGLE,
                                        result -> {
                                            if (result) {
                                                Log.d(LOG_TAG, "Firebase: Google account exists in Firebase");
                                                String googleIdToken = googleSignInAccount.getIdToken();
                                                AuthCredential authCredential =
                                                        GoogleAuthProvider.getCredential(googleIdToken, null);
                                                loginFirebaseWithCredential(authCredential);
                                            } else {
                                                Log.d(LOG_TAG, "Firebase: Google account not exists in Firebase");
                                                ErrorDialog errorDialog = new ErrorDialog(
                                                        LoginActivity.this,
                                                        "Đăng nhập không thành công",
                                                        "Tài khoản không tồn tại hoặc email đã được sử dụng cho phương thức đăng nhập khác"
                                                );
                                                errorDialog.show();
                                            }
                                        }
                                );
                            } else {
                                Log.d(LOG_TAG, "Google: Account get failed, return null");
                            }
                        }).addOnFailureListener(exception -> {
                            Log.d(LOG_TAG, "Google: sign in task failed");
                            exception.printStackTrace();
                            dialog.dismiss();
                        });
                    } else {
                        Log.d(LOG_TAG,
                                "Google: Google account pick launch failed, result code " +
                                        activityResult.getResultCode());
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
        Log.d(LOG_TAG, "Firebase auth: Get google sign in intent");
        getGoogleAccountActivityLauncher.launch(intent);
        googleSignInClient.signOut();
    }

    private void initLoginWithFacebook() {
        loginManager = LoginManager.getInstance();
        this.callbackManager = CallbackManager.Factory.create();
        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(LOG_TAG, "Facebook: Callback success");
                firebaseAuthHelper.isFacebookUserExistsInFirebase(loginResult,
                        result -> {
                            if (result) {
                                Log.d(LOG_TAG, "Firebase auth: Facebook user exists in Firebase");
                                AuthCredential credential =
                                        FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
                                loginFirebaseWithCredential(credential);
                            } else  {
                                Log.d(LOG_TAG, "Firebase auth: Facebook user not exists in Firebase");
                                ErrorDialog errorDialog = new ErrorDialog(
                                        LoginActivity.this,
                                        "Đăng nhập không thành công",
                                        "Tài khoản không tồn tại hoặc email đã được sử dụng cho phương thức đăng nhập khác"
                                );
                                errorDialog.show();
                            }
                        });
            }

            @Override
            public void onCancel() {
                Log.d(LOG_TAG, "Firebase auth: Facebook callback is cancelled");
                Log.d("FACEBOOK", "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                error.printStackTrace();
                Log.d(LOG_TAG, "Firebase auth: Facebook callback failed\n" + error.toString());
            }
        });
    }

    /**
     * Xử lý login bằng Facebook
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("FACEBOOK", "activity result");
        //  Gọi callback manager để xử lý dữ liệu Facebook trả về
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}