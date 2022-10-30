package com.uet.fwork.account;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.uet.fwork.LoadingScreenDialog;
import com.uet.fwork.R;
import com.uet.fwork.adapter.SpinnerAdapter;
import com.uet.fwork.database.model.CandidateModel;
import com.uet.fwork.database.repository.UserRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateCandidateProfileFragment extends Fragment {

    private EditText edtFullName, edtPhoneNumber, edtWorkEmail, edtMajor;
    private Spinner spnSex, spnYearExperience;
    private Button btnSubmit;
    private ImageView imgCamera;
    private CircleImageView cirImgAvatar;
    private NavController navController;
    private ActivityResultLauncher<Intent> getImageActivityLauncher;
    private Uri avatarImageUri = null;

    private final FirebaseAuth firebaseAuth;
    private final FirebaseStorage firebaseStorage;
    private final UserRepository userRepository;

    public CreateCandidateProfileFragment() {
        super(R.layout.fragment_enter_profile);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        userRepository = new UserRepository(FirebaseDatabase.getInstance());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getImageActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            avatarImageUri = data.getData();
                            Bitmap selectedImageBitmap = null;
                            try {
                                selectedImageBitmap
                                        = MediaStore.Images.Media.getBitmap(
                                        getActivity().getContentResolver(),
                                        avatarImageUri);
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                            cirImgAvatar.setImageBitmap(selectedImageBitmap);
                        }
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtFullName = view.findViewById(R.id.edtFullName);
        edtPhoneNumber = view.findViewById(R.id.edtPhoneNumber);
        edtWorkEmail = view.findViewById(R.id.edtWorkEmail);
        edtMajor = view.findViewById(R.id.edtMajor);
        spnSex = view.findViewById(R.id.spnSex);
        spnYearExperience = view.findViewById(R.id.spnYearExperience);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        imgCamera = view.findViewById(R.id.imgCamera);
        cirImgAvatar = view.findViewById(R.id.cirImgAvatar);
        navController = Navigation.findNavController(getActivity(), R.id.navigation_host);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(), R.array.sex_list, R.layout.custom_spinner
        );
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
        spnSex.setPadding(0, spnSex.getPaddingTop(), spnSex.getPaddingRight(), spnSex.getPaddingBottom());
        spnSex.setAdapter(adapter);

        List<Double> yearExperienceList = new ArrayList<>();
        yearExperienceList.add(0.5);
        yearExperienceList.add(1.0);
        yearExperienceList.add(2.0);
        yearExperienceList.add(3.0);
        yearExperienceList.add(5.0);
        yearExperienceList.add(7.0);
        yearExperienceList.add(10.0);
        SpinnerAdapter<Double> spinnerAdapter = new SpinnerAdapter<>(
                getContext(), yearExperienceList, R.layout.custom_spinner_2,
                (itemView, position) -> {
                    TextView txtView = itemView.findViewById(R.id.txtView);
                    if (yearExperienceList.get(position) < 1.0) {
                        txtView.setText("6 tháng");
                    } else {
                        txtView.setText((int) yearExperienceList.get(position).doubleValue() + " năm");
                    }
                }
        );
        spnYearExperience.setAdapter(spinnerAdapter);

        imgCamera.setOnClickListener(imgCameraView -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            getImageActivityLauncher.launch(intent);
        });

        btnSubmit.setOnClickListener(btnSubmitView -> {
            submitUserData();
            uploadAvatarImage();
        });
    }

    private void submitUserData() {
        String fullName = edtFullName.getText().toString();
        String phoneNumber = edtPhoneNumber.getText().toString();
        String workEmail = edtWorkEmail.getText().toString();
        String sex = spnSex.getSelectedItem().toString();
        String major = edtMajor.getText().toString();
        double yearOfExperience = (Double) spnYearExperience.getSelectedItem();

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        String userUID = firebaseUser.getUid();

        CandidateModel candidateModel = new CandidateModel(
                userUID, firebaseUser.getEmail(), "",
                fullName, phoneNumber, workEmail, sex, "26/10/2002",
                major, yearOfExperience
        );
        userRepository.insertUser(candidateModel);
    }

    /**
     * Upload ảnh lên Firebase Storage và update database của user
     */
    private void uploadAvatarImage() {
        if (avatarImageUri == null) {
            Navigation.findNavController(getActivity(), R.id.navigation_host)
                    .navigate(R.id.action_registerCreateProfileFragment_to_registerVerifyDoneFragment);
            return;
        }

        LoadingScreenDialog loadingScreenDialog = new LoadingScreenDialog(getContext());
        loadingScreenDialog.show();

        StorageReference storageReference = firebaseStorage.getReference("users/avatars");
        StorageReference imageReference = storageReference.child(firebaseAuth.getUid());
        imageReference.putFile(avatarImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageReference.getDownloadUrl().addOnCompleteListener(task -> {
                        String userUID = firebaseAuth.getCurrentUser().getUid();
                        Map<String, Object> updateData = new HashMap<>();
                        updateData.put("avatar", task.getResult().toString());
                        userRepository.updateUser(
                                userUID,
                                updateData
                        );
                        firebaseAuth.signOut();
                        loadingScreenDialog.dismiss();
                        //  Chuyển hướng sang màn hình thông báo tạo tài khoản thành công
                        navController.navigate(R.id.action_registerCreateProfileFragment_to_registerVerifyDoneFragment);
                    });
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }
}
