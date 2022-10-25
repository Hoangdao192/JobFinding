package com.uet.fwork;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.uet.fwork.user.Role;

import java.util.HashMap;
import java.util.Map;

public class RegisterMainFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;

    private TextInputLayout edtEmail;
    private TextInputLayout edtPassword, edtRePassword;
    private Button btnCreateAccount;
    private RadioGroup radGroupRole;
    private RadioButton radEmployee, radEmployer;
    private TextView txtIam;

    public RegisterMainFragment() {
        super(R.layout.fragment_register_main);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance(Constants.DATABASE_URL);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        edtEmail = view.findViewById(R.id.edtEmail);
        edtPassword = view.findViewById(R.id.edtPassword);
        edtRePassword = view.findViewById(R.id.edtRePassword);
        btnCreateAccount = view.findViewById(R.id.btnRegister);
        radGroupRole = view.findViewById(R.id.radGroupRole);
        radEmployee = view.findViewById(R.id.radEmployee);
        radEmployer = view.findViewById(R.id.radEmployer);
        txtIam = view.findViewById(R.id.txtIam);

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetViewError();

                String email = edtEmail.getEditText().getText().toString();
                String password = edtPassword.getEditText().getText().toString();
                String rePassword = edtRePassword.getEditText().getText().toString();

                if (radGroupRole.getCheckedRadioButtonId() == -1) {
                    txtIam.setText(R.string.you_are_error);
                    return;
                }

                if (email.length() == 0) {
                    edtEmail.setError("Enter your email.");
                    edtEmail.requestFocus();
                    return;
                }

                if (password.length() == 0) {
                    edtPassword.setError("Enter your password.");
                    edtPassword.requestFocus();
                    return;
                }

                if (rePassword.length() == 0) {
                    edtRePassword.setError("Confirm your password.");
                    edtRePassword.requestFocus();
                    return;
                }

                if (!password.equals(rePassword)) {
                    edtRePassword.setError("Password does not match.");
                    edtRePassword.requestFocus();
                    return;
                }

                createUserAccount(email, password);

//                firebaseAuth.createUserWithEmailAndPassword(email, password)
//                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
//                            @Override
//                            public void onSuccess(AuthResult authResult) {
//                                LoadingScreenDialog dialog = new LoadingScreenDialog(getContext());
//                                dialog.show();
//                                firebaseAuth.getCurrentUser().sendEmailVerification()
//                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                            @Override
//                                            public void onSuccess(Void unused) {
//                                                createUserData();
//                                                dialog.dismiss();
//                                                Navigation.findNavController(getActivity(), R.id.navigation_host)
//                                                        .navigate(R.id.action_registerMainFragment_to_registerVerifyRequestFragment);
//                                            }
//                                        });
//                            }
//                        });
//                Navigation.findNavController(getActivity(), R.id.navigation_host)
//                        .navigate(R.id.action_registerMainFragment_to_registerVerifyRequestFragment);
            }
        });
    }

    private void resetViewError() {
        edtEmail.setErrorEnabled(false);
        edtPassword.setErrorEnabled(false);
        edtRePassword.setErrorEnabled(false);
        txtIam.setText(R.string.you_are);
    }

    /**
     * Tạo tài khoản mới
     * @param email
     * @param password
     */
    private void createUserAccount(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        LoadingScreenDialog dialog = new LoadingScreenDialog(getContext());
                        dialog.show();
                        firebaseAuth.getCurrentUser().sendEmailVerification()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        createUserData();
                                        dialog.dismiss();
                                        Navigation.findNavController(getActivity(), R.id.navigation_host)
                                                .navigate(R.id.action_registerMainFragment_to_registerVerifyRequestFragment);
                                    }
                                });
                    }
                });
    }

    //  Khởi tạo data ban đầu cho user
    private void createUserData() {
        String email = edtEmail.getEditText().getText().toString();
        String role = "";
        String userUID = firebaseAuth.getCurrentUser().getUid();

        switch (radGroupRole.getCheckedRadioButtonId()) {
            case R.id.radEmployee: role = Role.CANDIDATE; break;
            case R.id.radEmployer: role = Role.EMPLOYER; break;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", userUID);
        data.put("email", email);
        data.put("role", role);
        data.put("fullName", "");
        data.put("phoneNumber", "");
        data.put("sex", "");
        data.put("avatar", "");
        data.put("major", "");

        firebaseDatabase.getReference("users")
                .child(userUID).setValue(data);
    }
}