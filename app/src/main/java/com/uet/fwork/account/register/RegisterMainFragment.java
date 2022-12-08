package com.uet.fwork.account.register;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.uet.fwork.dialog.ErrorDialog;
import com.uet.fwork.dialog.LoadingScreenDialog;
import com.uet.fwork.R;
import com.uet.fwork.account.login.LoginActivity;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.model.UserRole;
import com.uet.fwork.database.repository.UserRepository;
import com.uet.fwork.firebasehelper.FirebaseAuthHelper;

import java.util.Arrays;

public class RegisterMainFragment extends Fragment {
    private static final String LOG_TAG = "Register";

    private NavController navController;
    private TextInputLayout edtEmail;
    private TextInputLayout edtPassword, edtRePassword;
    private Button btnCreateAccount;
    private ImageButton btnBack;
    private TextView txtLogin;
    private Button btnRegisterWithGoogle, btnRegisterWithFacebook;

    private final FirebaseAuthHelper firebaseAuthHelper;
    private final FirebaseAuth firebaseAuth;
    private final UserRepository userRepository;

    //  Facebook login
    private CallbackManager callbackManager;
    private LoginManager loginManager;
    //  Google login
    private ActivityResultLauncher<Intent> getGoogleAccountActivityLauncher;

    public RegisterMainFragment() {
        super(R.layout.fragment_register_main);
        firebaseAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository(FirebaseDatabase.getInstance());
        firebaseAuthHelper = new FirebaseAuthHelper(firebaseAuth);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        edtEmail = view.findViewById(R.id.edtEmail);
        edtPassword = view.findViewById(R.id.edtPassword);
        edtRePassword = view.findViewById(R.id.edtRePassword);
        btnCreateAccount = view.findViewById(R.id.btnRegister);
        txtLogin = view.findViewById(R.id.txtLogin);
        btnRegisterWithGoogle = view.findViewById(R.id.relLoginGoogle);
        btnRegisterWithFacebook = view.findViewById(R.id.btnLoginFacebook);
        navController = Navigation.findNavController(getActivity(), R.id.navigation_host);
        btnBack = view.findViewById(R.id.btnBack);

        initRegisterWithGoogle();
        initRegisterWithFacebook();

        btnBack.setOnClickListener(button -> getActivity().finish());

        txtLogin.setOnClickListener(textView -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });

        btnCreateAccount.setOnClickListener(button -> {
            clearTextInputLayoutError();
            if (checkUserInput()) {
                createUserAccount();
            }
        });

        btnRegisterWithGoogle.setOnClickListener(button -> registerWithGoogle());

        btnRegisterWithFacebook.setOnClickListener(button -> {
            Log.d(LOG_TAG, "Firebase auth: Start register facebook");
            loginManager.logInWithReadPermissions(
                    getActivity(),
                    Arrays.asList("email", "public_profile"));
        });

        Intent intent = getActivity().getIntent();
        Log.d(LOG_TAG, "Checking activity destination");
        if (intent.hasExtra("startDestinationId")) {
            navController.navigate(intent.getIntExtra("startDestinationId", R.id.registerMainFragment));
        }
    }

    private boolean checkUserInput() {
        String email = edtEmail.getEditText().getText().toString();
        String password = edtPassword.getEditText().getText().toString();
        String rePassword = edtRePassword.getEditText().getText().toString();

        if (email.length() == 0) {
            edtEmail.setError("Enter your email.");
            edtEmail.requestFocus();
            return false;
        }

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

    private void clearTextInputLayoutError() {
        edtEmail.setErrorEnabled(false);
        edtPassword.setErrorEnabled(false);
        edtRePassword.setErrorEnabled(false);
    }

    /**
     * Tạo tài khoản mới và chuyển hướng sang màn hình xác thực email
     */
    private void createUserAccount() {
        String email = edtEmail.getEditText().getText().toString();
        String password = edtPassword.getEditText().getText().toString();

        LoadingScreenDialog dialog = new LoadingScreenDialog(getContext());
        dialog.show();
        Log.d(LOG_TAG, "Firebase auth: Create user with email and password");
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Log.d(LOG_TAG, "Firebase auth: Create user successful " + email + " " + password);
                    firebaseAuth.getCurrentUser()
                        .sendEmailVerification()
                        .addOnFailureListener(exception -> {
                            exception.printStackTrace();
                            Log.d(LOG_TAG, "Firebase auth: Send email verification to " + email + " failed");
                        })
                        .addOnCanceledListener(() ->
                                Log.d(LOG_TAG, "Firebase auth: Send email verification to " + email + " cancelled"))
                        .addOnSuccessListener(unused -> {
                            Log.d(LOG_TAG, "Firebase auth: Send email verification to " + email + " successful");
                            initUserData();
                            dialog.dismiss();
                            navController.navigate(R.id.action_registerMainFragment_to_registerVerifyRequestFragment);
                        });
                })
                .addOnFailureListener(exception -> {
                    exception.printStackTrace();
                    Log.d(LOG_TAG, "Firebase auth: Create user failed " + email + " " + password);
                    if (exception instanceof FirebaseAuthUserCollisionException) {
                        edtEmail.setError("Email đã được sử dụng.");
                    } else if (exception instanceof FirebaseAuthWeakPasswordException) {
                        edtPassword.setError("Mật khẩu phải chứa ít nhất 6 kí tự");
                    }
                    dialog.dismiss();
                })
                .addOnCanceledListener(() ->
                        Log.d(LOG_TAG, "Firebase auth: Create user cancelled " + email + " " + password));
    }

    /**
     *  Khởi tạo dữ liệu ban đầu cho user
     */
    private void initUserData() {
        String email = firebaseAuth.getCurrentUser().getEmail();
        String userUID = firebaseAuth.getCurrentUser().getUid();
        Log.d(LOG_TAG, "Initialize user data");
        UserModel userModel = new UserModel(
                userUID, email, "", "", "", "", UserRole.NOT_SET, 0
        );
        userRepository.insertUser(userModel);
    }

    private void loginFirebaseWithCredential(AuthCredential authCredential) {
        LoadingScreenDialog dialog = new LoadingScreenDialog(getContext());
        dialog.show();
        Log.d(LOG_TAG, "Firebase auth: Login with credential " + authCredential.toString());
        firebaseAuth.signInWithCredential(authCredential)
                .addOnSuccessListener(authResult -> {
                    Log.d(LOG_TAG, "Firebase auth: Login with credential successful " + authCredential.toString());
                    dialog.dismiss();
                    initUserData();
                    navController.navigate(R.id.action_registerMainFragment_to_selectUserRoleFragment);
                })
                .addOnFailureListener(exception -> {
                    Log.d(LOG_TAG, "Firebase auth: Login with credential failed " + authCredential.toString());
                    exception.printStackTrace();
                    Toast.makeText(getActivity(), "Email của tài khoản này đã được sử dụng", Toast.LENGTH_SHORT).show();
                })
                .addOnCanceledListener(() ->
                        Log.d(LOG_TAG, "Firebase auth: Login with credential cancelled " + authCredential.toString()));
    }

    private void initRegisterWithGoogle() {
        Log.d(LOG_TAG, "Firebase auth: Initialize register with google");
        getGoogleAccountActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                activityResult -> {
                    if (activityResult.getResultCode() == Activity.RESULT_OK) {
                        Log.d(LOG_TAG, "Google: Google account pick launch ok");
                        LoadingScreenDialog dialog = new LoadingScreenDialog(getActivity());
                        dialog.show();

                        Log.d(LOG_TAG, "Google: Get sign in account task");
                        Task<GoogleSignInAccount> signInAccountTask =
                                GoogleSignIn.getSignedInAccountFromIntent(activityResult.getData());

                        signInAccountTask.addOnSuccessListener(googleSignInAccount -> {
                            Log.d(LOG_TAG, "Google: sign in task successful");
                            if (googleSignInAccount != null) {
                                String email = googleSignInAccount.getEmail();
                                Log.d(LOG_TAG, "Google: Account get successful " + email);
                                firebaseAuthHelper.isUserWithEmailExists(
                                        email,
                                        result -> {
                                            Log.d(LOG_TAG, "Firebase: Google account exists in Firebase");
                                            dialog.dismiss();
                                            if (!result) {
                                                String googleIdToken = googleSignInAccount.getIdToken();
                                                AuthCredential authCredential =
                                                        GoogleAuthProvider.getCredential(googleIdToken, null);
                                                loginFirebaseWithCredential(authCredential);
                                            } else {
                                                Log.d(LOG_TAG, "Firebase: Google account not exists in Firebase");
                                                ErrorDialog errorDialog = new ErrorDialog(
                                                        getActivity(),
                                                        "Đăng ký không thành công",
                                                        "Tài khoản không tồn tại hoặc email đã được sử dụng cho phương thức đăng nhập khác"
                                                );
                                                errorDialog.show();
                                            }
                                        }
                                );
                            } else {
                                dialog.dismiss();
                                Log.d(LOG_TAG, "Google: Account get failed, return null");
                            }
                        }).addOnFailureListener(exception -> {
                                Log.e(LOG_TAG, "Google: sign in task failed");
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

    private void registerWithGoogle() {
        String oAuthToken = getString(R.string.default_web_client_id);
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(oAuthToken)
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getActivity(), googleSignInOptions);
        Log.d(LOG_TAG, "Firebase auth: Get google sign in intent");
        Intent intent = googleSignInClient.getSignInIntent();
        getGoogleAccountActivityLauncher.launch(intent);
        googleSignInClient.signOut();
    }

    private void initRegisterWithFacebook() {
        Log.d(LOG_TAG, "Firebase auth: Initialize register with facebook");
        loginManager = LoginManager.getInstance();
        this.callbackManager = ((RegisterActivity) getActivity()).callbackManager;
        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(LOG_TAG, "Facebook: Callback success");
                firebaseAuthHelper.isFacebookUserExistsInFirebase(loginResult,
                        result -> {
                            if (!result) {
                                Log.d(LOG_TAG, "Firebase auth: Facebook user exists in Firebase");
                                AuthCredential credential =
                                        FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
                                loginFirebaseWithCredential(credential);
                            } else  {
                                Log.d(LOG_TAG, "Firebase auth: Facebook user not exists in Firebase");
                                ErrorDialog errorDialog = new ErrorDialog(
                                        getActivity(),
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