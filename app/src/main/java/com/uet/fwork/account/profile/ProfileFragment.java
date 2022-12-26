package com.uet.fwork.account.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.uet.fwork.R;
import com.uet.fwork.account.ChangePasswordActivity;
import com.uet.fwork.account.login.LoginActivity;
import com.uet.fwork.database.model.CandidateModel;
import com.uet.fwork.database.model.EmployerModel;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.model.UserRole;
import com.uet.fwork.database.repository.UserRepository;
import com.uet.fwork.firebasehelper.FirebaseAuthHelper;
import com.uet.fwork.firebasehelper.FirebaseSignInMethod;

public class ProfileFragment extends Fragment{

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseAuthHelper firebaseAuthHelper;
    private UserRepository userRepository;

    private ImageView imgAvatar;
    private TextView txvName, txvEmail, txvPhone, txvSex, txvBirth, txvYearOfExperience, txvMajor;
    private TextView txvCompanyDescription, txvAddress;

    private String userRole = "";
    private UserModel user;

    public ProfileFragment() {
        super(R.layout.fragment_profile_candidate);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuthHelper = FirebaseAuthHelper.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userRepository = UserRepository.getInstance();
        userRole = getArguments().getString("USER_ROLE");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (userRole.equals(UserRole.CANDIDATE)) {
            return inflater.inflate(R.layout.fragment_profile_candidate, container, false);
        } else if (userRole.equals(UserRole.EMPLOYER)) {
            return inflater.inflate(R.layout.fragment_profile_employer, container, false);
        }
        return null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        Button btnLogout = view.findViewById(R.id.return_button);
//        btnLogout.setOnClickListener(button -> {
//                firebaseAuthHelper.signOut();
//                Intent intent = new Intent(getActivity(), LoginActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//                getActivity().finish();
//            });

        TextView txtEditProfile = view.findViewById(R.id.edtProfile_button);
        txtEditProfile.setOnClickListener(editText -> {
            if (userRole.equals(UserRole.CANDIDATE)) {
                Intent intent = new Intent(getActivity(), UpdateCandidateProfileActivity.class);
                intent.putExtra("CANDIDATE", (CandidateModel) user);
                startActivity(intent);
            } else if (userRole.equals(UserRole.EMPLOYER)) {
                Intent intent = new Intent(getActivity(), UpdateEmployerProfileActivity.class);
                intent.putExtra("EMPLOYER", (EmployerModel) user);
                startActivity(intent);
            }
        });

        imgAvatar = view.findViewById(R.id.avatarIv);
        if (userRole.equals(UserRole.CANDIDATE)) {
            txvName = view.findViewById(R.id.nameTv);
            txvBirth = view.findViewById(R.id.birthTv);
            txvSex = view.findViewById(R.id.sexTv);
            txvMajor = view.findViewById(R.id.jobTv);
            txvYearOfExperience = view.findViewById(R.id.workYearTv);
            txvPhone = view.findViewById(R.id.phoneTv);
            txvEmail = view.findViewById(R.id.emailTv);
        } else if (userRole.equals(UserRole.EMPLOYER)) {
            txvName = view.findViewById(R.id.nameTv);
            txvPhone = view.findViewById(R.id.phoneTv);
            txvEmail = view.findViewById(R.id.emailTv);
            txvAddress = view.findViewById(R.id.txtAddress);
            txvCompanyDescription = view.findViewById(R.id.txtDescription);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        userRepository.getUserByUID(firebaseUser.getUid(), userModel -> {
            if (userModel != null) {
                user = userModel;

                String fullName = userModel.getFullName();
                String contactEmail = userModel.getContactEmail();
                String phoneNumber = userModel.getPhoneNumber();

                String avatarImagePath = userModel.getAvatar();
                if (!avatarImagePath.isEmpty()) {
                    Picasso.get().load(avatarImagePath)
                            .placeholder(R.drawable.wlop_33se)
                            .into(imgAvatar);
                }

                //  Ứng viên
                if (userModel instanceof CandidateModel) {
                    CandidateModel candidate = (CandidateModel) userModel;
                    txvName.setText(fullName);
                    txvEmail.setText(contactEmail);
                    txvPhone.setText(phoneNumber);
                    txvBirth.setText(candidate.getDateOfBirth());
                    txvSex.setText(candidate.getSex());
                    txvMajor.setText(candidate.getMajor());
                    txvYearOfExperience.setText(String.valueOf(candidate.getYearOfExperience()));
                } else if (userModel instanceof EmployerModel) {
                    EmployerModel employer = (EmployerModel) userModel;
                    txvName.setText(fullName);
                    txvEmail.setText(contactEmail);
                    txvPhone.setText(phoneNumber);
                    txvAddress.setText(employer.getAddress().toString());
                    txvCompanyDescription.setText(getString(R.string.company_description, employer.getDescription()));
                }
            }
        });
    }
}