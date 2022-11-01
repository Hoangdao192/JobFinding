package com.uet.fwork.account.register;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
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
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.uet.fwork.HelloActivity;
import com.uet.fwork.LoadingScreenDialog;
import com.uet.fwork.R;
import com.uet.fwork.account.login.LoginActivity;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.model.UserRole;
import com.uet.fwork.database.repository.UserRepository;
import com.uet.fwork.firebasehelper.FirebaseAuthHelper;
import com.uet.fwork.firebasehelper.FirebaseSignInMethod;

import java.util.Arrays;

public class RegisterMainFragment extends Fragment {

    private final FirebaseAuth firebaseAuth;
    private final UserRepository userRepository;

    private NavController navController;
    private TextInputLayout edtEmail;
    private TextInputLayout edtPassword, edtRePassword;
    private Button btnCreateAccount;
    private TextView txtLogin;
    private RelativeLayout btnRegisterWithGoogle;
    private RelativeLayout btnRegisterWithFacebook;

    private FirebaseAuthHelper firebaseAuthHelper;

    private CallbackManager callbackManager;
    private LoginManager loginManager;

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

        initRegisterWithGoogle();
        initRegisterWithFacebook();

        txtLogin.setOnClickListener(txtLoginView -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });

        btnCreateAccount.setOnClickListener(btnCreateAccountView -> {
            clearTextInputLayoutError();
            if (checkUserInput()) {
                createUserAccount();
            }
        });

        btnRegisterWithGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerWithGoogle();
            }
        });

        btnRegisterWithFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginManager.logInWithReadPermissions(getActivity(), Arrays.asList("email", "public_profile"));
            }
        });

        Intent intent = getActivity().getIntent();
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
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> firebaseAuth.getCurrentUser()
                        .sendEmailVerification()
                        .addOnSuccessListener(unused -> {
                            initUserData();
                            dialog.dismiss();
                            navController.navigate(R.id.action_registerMainFragment_to_registerVerifyRequestFragment);
                        }))
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        edtEmail.setError("Email đã được sử dụng.");
                    } else if (e instanceof FirebaseAuthWeakPasswordException) {
                        edtPassword.setError("Mật khẩu phải chứa ít nhất 6 kí tự");
                    }
                    dialog.dismiss();
                });
    }

    /**
     *  Khởi tạo dữ liệu ban đầu cho user
     */
    private void initUserData() {
        String email = firebaseAuth.getCurrentUser().getEmail();
        String userUID = firebaseAuth.getCurrentUser().getUid();

        UserModel userModel = new UserModel(
                userUID, email, "", "", "", "", UserRole.NOT_SET, 0
        );
        userRepository.insertUser(userModel);
    }

    private void loginFirebaseWithCredential(AuthCredential authCredential) {
        firebaseAuth.signInWithCredential(authCredential)
                .addOnSuccessListener(authResult -> {
                    initUserData();
                    navController.navigate(R.id.action_registerMainFragment_to_selectUserRoleFragment);
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    private void initRegisterWithGoogle() {
        getGoogleAccountActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                activityResult -> {
                    if (activityResult.getResultCode() == Activity.RESULT_OK) {
                        Task<GoogleSignInAccount> signInAccountTask =
                                GoogleSignIn.getSignedInAccountFromIntent(activityResult.getData());

                        signInAccountTask.addOnSuccessListener(googleSignInAccount -> {
                            if (googleSignInAccount != null) {
                                String email = googleSignInAccount.getEmail();
                                firebaseAuthHelper.isUserWithEmailExists(
                                        email,
                                        result -> {
                                            if (!result) {
                                                String googleIdToken = googleSignInAccount.getIdToken();
                                                AuthCredential authCredential =
                                                        GoogleAuthProvider.getCredential(googleIdToken, null);
                                                loginFirebaseWithCredential(authCredential);
                                            } else {
                                                //  UI thông báo lỗi
                                                Toast.makeText(getActivity(),
                                                                "Tài khoản này đã tồn tại", Toast.LENGTH_SHORT)
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

    private void registerWithGoogle() {
        String oAuthToken = getString(R.string.default_web_client_id);
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(oAuthToken)
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getActivity(), googleSignInOptions);
        Intent intent = googleSignInClient.getSignInIntent();
        getGoogleAccountActivityLauncher.launch(intent);
        googleSignInClient.signOut();
    }

    private void initRegisterWithFacebook() {
        loginManager = LoginManager.getInstance();
        this.callbackManager = ((RegisterActivity) getActivity()).callbackManager;
        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("FACEBOOK", "Callback success");
                firebaseAuthHelper.isFacebookUserExistsInFirebase(loginResult,
                        result -> {
                            if (!result) {
                                AuthCredential credential =
                                        FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
                                loginFirebaseWithCredential(credential);
                            } else  {
                                //  UI thông báo tài khoản không tồn tại
                                Toast.makeText(getActivity(),
                                                "Tài khoản này đã tồn tại", Toast.LENGTH_SHORT)
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