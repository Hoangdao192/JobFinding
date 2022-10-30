package com.uet.fwork.account.register;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.FirebaseDatabase;
import com.uet.fwork.LoadingScreenDialog;
import com.uet.fwork.R;
import com.uet.fwork.account.login.LoginActivity;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.model.UserRole;
import com.uet.fwork.database.repository.UserRepository;

public class RegisterMainFragment extends Fragment {

    private final FirebaseAuth firebaseAuth;
    private final UserRepository userRepository;

    private NavController navController;
    private TextInputLayout edtEmail;
    private TextInputLayout edtPassword, edtRePassword;
    private Button btnCreateAccount;
    private RadioGroup radGroupRole;
    private TextView txtIam;
    private TextView txtLogin;

    public RegisterMainFragment() {
        super(R.layout.fragment_register_main);
        firebaseAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository(FirebaseDatabase.getInstance());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        edtEmail = view.findViewById(R.id.edtEmail);
        edtPassword = view.findViewById(R.id.edtPassword);
        edtRePassword = view.findViewById(R.id.edtRePassword);
        btnCreateAccount = view.findViewById(R.id.btnRegister);
        radGroupRole = view.findViewById(R.id.radGroupRole);
        txtIam = view.findViewById(R.id.txtIam);
        txtLogin = view.findViewById(R.id.txtLogin);
        navController = Navigation.findNavController(getActivity(), R.id.navigation_host);

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
    }

    private String getSelectedUserRole() {
        String role = "";
        switch (radGroupRole.getCheckedRadioButtonId()) {
            case R.id.radEmployee: role = UserRole.CANDIDATE; break;
            case R.id.radEmployer: role = UserRole.EMPLOYER; break;
        }
        return role;
    }

    private boolean checkUserInput() {
        String email = edtEmail.getEditText().getText().toString();
        String password = edtPassword.getEditText().getText().toString();
        String rePassword = edtRePassword.getEditText().getText().toString();

        if (radGroupRole.getCheckedRadioButtonId() == -1) {
            txtIam.setText(R.string.you_are_error);
            return false;
        }

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
        txtIam.setText(R.string.you_are);
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

                            Bundle bundle = new Bundle();
                            bundle.putString("role", getSelectedUserRole());
                            //  Chuyển hướng sang màn hình xác thực email
                            navController.navigate(
                                    R.id.action_registerMainFragment_to_registerVerifyRequestFragment,
                                    bundle
                            );
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
        String email = edtEmail.getEditText().getText().toString();
        String role = getSelectedUserRole();
        String userUID = firebaseAuth.getCurrentUser().getUid();

        UserModel userModel = new UserModel(
                userUID, email, "", "", "", "", role
        );
        userRepository.insertUser(userModel);
    }
}