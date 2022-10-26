package com.uet.fwork;

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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.uet.fwork.database.model.CandidateModel;
import com.uet.fwork.database.model.EmployerModel;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.model.UserRole;
import com.uet.fwork.database.repository.UserRepository;
import com.uet.fwork.user.Role;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterCreateProfileFragment extends Fragment {

    private EditText edtFullName, edtPhoneNumber, edtWorkEmail, edtMajor;
    private Spinner spnSex;
    private Button btnSubmit;
    private ImageView imgCamera;
    private CircleImageView cirImgAvatar;
    private ActivityResultLauncher<Intent> mGetImage;
    private Uri avatarImageUri;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private FirebaseStorage firebaseStorage;
    private UserRepository userRepository;

    public RegisterCreateProfileFragment() {
        super(R.layout.fragment_enter_profile);
        databaseReference = FirebaseDatabase.getInstance(Constants.DATABASE_URL).getReference("/users");
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        userRepository = new UserRepository(FirebaseDatabase.getInstance());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mGetImage = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            // do your operation from here....
                            if (data != null
                                    && data.getData() != null) {
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
        btnSubmit = view.findViewById(R.id.btnSubmit);
        imgCamera = view.findViewById(R.id.imgCamera);
        cirImgAvatar = view.findViewById(R.id.cirImgAvatar);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(), R.array.sex_list, R.layout.custom_spinner
        );
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
        spnSex.setPadding(0, spnSex.getPaddingTop(), spnSex.getPaddingRight(), spnSex.getPaddingBottom());
        spnSex.setAdapter(adapter);

        imgCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
//                getActivity().startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);

                mGetImage.launch(intent);
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitUserData();
                Navigation.findNavController(getActivity(), R.id.navigation_host)
                        .navigate(R.id.action_registerCreateProfileFragment_to_registerVerifyDoneFragment);
            }
        });
    }

    private void submitUserData() {
        String fullName = edtFullName.getText().toString();
        String phoneNumber = edtPhoneNumber.getText().toString();
        String workEmail = edtWorkEmail.getText().toString();
        String sex = spnSex.getSelectedItem().toString();
        String major = edtMajor.getText().toString();

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        String userUID = firebaseUser.getUid();

        CandidateModel candidateModel = new CandidateModel(
                userUID, firebaseUser.getEmail(), "",
                fullName, phoneNumber, workEmail, sex, "26/10/2002"
        );
        userRepository.insertUser(candidateModel);

        uploadAvatarImage();
    }

    private void uploadAvatarImage() {
        LoadingScreenDialog loadingScreenDialog = new LoadingScreenDialog(getContext());
        loadingScreenDialog.show();
        StorageReference storageReference = firebaseStorage.getReference("users/avatars");
        StorageReference imageReference = storageReference.child(firebaseAuth.getCurrentUser().getUid());
        imageReference.putFile(avatarImageUri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isCanceled()) {
                            task.getException().printStackTrace();
                        } else {
                            imageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    String userUID = firebaseAuth.getCurrentUser().getUid();
                                    Map<String, Object> updateData = new HashMap<>();
                                    updateData.put("avatar", task.getResult().toString());
                                    userRepository.updateUser(
                                            userUID,
                                            updateData
                                    );
                                    loadingScreenDialog.dismiss();
                                }
                            });
                        }
                    }
                });
    }
}
