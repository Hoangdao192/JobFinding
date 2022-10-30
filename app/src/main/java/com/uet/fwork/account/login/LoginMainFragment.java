package com.uet.fwork.account.login;

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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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
import com.uet.fwork.HelloActivity;
import com.uet.fwork.R;
import com.uet.fwork.account.register.RegisterActivity;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.repository.UserRepository;

public class LoginMainFragment extends Fragment implements ActivityResultCallback<ActivityResult>{

    private final FirebaseAuth firebaseAuth;

    private NavController navController;
    private TextInputLayout edtEmail, edtPassword;
    private Button btnLogin;
    private RelativeLayout btnLoginWithGoogle;
    private TextView txtCreateAccount;

    private ActivityResultLauncher<Intent> getGoogleAccountActivityLauncher;

    private final UserRepository userRepository;

    public LoginMainFragment() {
        super(R.layout.fragment_login_main);
        firebaseAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository(FirebaseDatabase.getInstance());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getGoogleAccountActivityLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtEmail = (TextInputLayout) view.findViewById(R.id.edtEmail);
        edtPassword = (TextInputLayout) view.findViewById(R.id.edtPassword);
        btnLogin = (Button) view.findViewById(R.id.btnLogin);
        btnLoginWithGoogle = (RelativeLayout) view.findViewById(R.id.relLoginGoogle);
        txtCreateAccount = (TextView) view.findViewById(R.id.txtCreateAccount);
        navController = Navigation.findNavController(getActivity(), R.id.navigation_host);

        btnLogin.setOnClickListener(btnLoginView -> loginWithEmailPassword());

        btnLoginWithGoogle.setOnClickListener(btnLoginWithGoogleView -> loginWithGoogle());

        txtCreateAccount.setOnClickListener(txtCreateAccountView -> {
            Intent intent = new Intent(getActivity(), RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void initUserData() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        UserModel userModel = new UserModel(
                firebaseUser.getUid(), firebaseUser.getEmail(),
                "", "", "", "", ""
        );
        userRepository.insertUser(userModel);
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

    private void loginWithGoogle() {
        String oAuthToken = getString(R.string.default_web_client_id);
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(oAuthToken)
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getContext(), googleSignInOptions);
        googleSignInClient.revokeAccess();
        Intent intent = googleSignInClient.getSignInIntent();
        getGoogleAccountActivityLauncher.launch(intent);
    }

    /**
     * Xư lý sau khi gọi Intent lấy Google account
     * @param activityResult
     */
    @Override
    public void onActivityResult(ActivityResult activityResult) {
        if (activityResult.getResultCode() == Activity.RESULT_OK) {
            Task<GoogleSignInAccount> signInAccountTask =
                    GoogleSignIn.getSignedInAccountFromIntent(activityResult.getData());

            signInAccountTask.addOnSuccessListener(googleSignInAccount -> {
                if (googleSignInAccount != null) {
                    String googleIdToken = googleSignInAccount.getIdToken();
                    AuthCredential authCredential =
                            GoogleAuthProvider.getCredential(googleIdToken, null);

                    /**
                     * Đăng nhập người dùng vào Firebase
                     * Nếu người dùng chưa tồn tại trong Database thì khởi tạo dữ liệu của người dùng
                     */
                    firebaseAuth.signInWithCredential(authCredential)
                            .addOnSuccessListener(authResult -> userRepository.isUserExists(
                                    firebaseAuth.getUid(),
                                    result -> {
                                        if (!result) {
                                            initUserData();
                                            //  Chuyển hướng sang màn hình chọn UserRole
                                            navController.navigate(R.id.action_loginMainFragment_to_selectUserRoleFragment);
                                        }
                                    }))
                            .addOnFailureListener(Throwable::printStackTrace);
                }
            });
        } else {
            System.out.println("RESULT CODE " + activityResult.getResultCode());
        }
    }
}
