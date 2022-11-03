package com.uet.fwork.account.resetpassword;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.uet.fwork.LoadingScreenDialog;
import com.uet.fwork.R;

public class ResetPasswordEnterEmailFragment extends Fragment {

    private TextInputLayout edtEmail;
    private Button btnSubmit;

    public ResetPasswordEnterEmailFragment() {
        super(R.layout.fragment_reset_password_main);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        edtEmail = view.findViewById(R.id.edtEmail);
        btnSubmit = view.findViewById(R.id.btnNext);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                String emailAddress = edtEmail.getEditText().getText().toString();
                LoadingScreenDialog dialog = new LoadingScreenDialog(getContext());
                dialog.show();
                firebaseAuth.sendPasswordResetEmail(emailAddress).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        dialog.dismiss();
                        Navigation.findNavController(getActivity(),
                                R.id.navigation_host)
                                .navigate(R.id.action_resetPasswordEnterEmailFragment_to_resetPasswordFragment);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }
}
