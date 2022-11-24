package com.uet.fwork.account.register;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.uet.fwork.dialog.LoadingScreenDialog;
import com.uet.fwork.R;
import com.uet.fwork.adapter.SpinnerAdapter;
import com.uet.fwork.database.model.CandidateModel;
import com.uet.fwork.database.repository.UserRepository;
import com.uet.fwork.util.ImageHelper;
import com.uet.fwork.util.ImagePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateCandidateProfileFragment extends Fragment {

    private EditText edtFullName, edtPhoneNumber, edtWorkEmail, edtDob;
    private Spinner spnSex, spnYearExperience, spnMajor;
    private Button btnSubmit;
    private ImageView imgCamera;
    private CircleImageView cirImgAvatar;
    private NavController navController;
    private ActivityResultLauncher<Intent> getImageActivityLauncher;
    private Uri avatarImageUri = null;
    private ImagePicker imagePicker;
    private Bitmap avatarImageBitmap = null;

    private List<String> majorList = new ArrayList<>();

    private Calendar calendar;

    private final FirebaseAuth firebaseAuth;
    private final FirebaseStorage firebaseStorage;
    private final UserRepository userRepository;
    private final FirebaseDatabase firebaseDatabase;

    public CreateCandidateProfileFragment() {
        super(R.layout.fragment_enter_profile);
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        userRepository = new UserRepository(FirebaseDatabase.getInstance());

        calendar = Calendar.getInstance();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getImageActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Bitmap bitmap = ImagePicker.getImageFromResult(getContext(), result);
                    if (bitmap != null) {
                        avatarImageBitmap = ImageHelper.reduceImageSize(bitmap, 400, 400);
                        cirImgAvatar.setImageBitmap(avatarImageBitmap);
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
        edtDob = view.findViewById(R.id.edtDob);
        spnSex = view.findViewById(R.id.spnSex);
        spnYearExperience = view.findViewById(R.id.spnYearExperience);
        spnMajor = view.findViewById(R.id.spnMajor);
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

        loadMajorList();

        edtDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(
                        getActivity(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                String dateOfBirth = dayOfMonth + "/" + (month + 1) + "/" + year;
                                edtDob.setText(dateOfBirth);
                            }
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                ).show();
            }
        });

        imgCamera.setOnClickListener(imgCameraView -> {
            Intent intent = ImagePicker.getPickImageIntent(getContext());
            getImageActivityLauncher.launch(intent);
        });

        btnSubmit.setOnClickListener(btnSubmitView -> {
            submitUserData();
            uploadAvatarImage();
        });
    }

    private void loadMajorList() {
        firebaseDatabase.getReference("userMajors")
                .get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        GenericTypeIndicator<List<String>> genericTypeIndicator =
                                new GenericTypeIndicator<List<String>>() {};
                        majorList.addAll(dataSnapshot.getValue(genericTypeIndicator));
                        SpinnerAdapter<String> spinnerMajorAdapter = new SpinnerAdapter<>(
                                getContext(), majorList, R.layout.custom_spinner_2,
                                (itemView, position) -> {
                                    TextView txtView = itemView.findViewById(R.id.txtView);
                                    txtView.setText(majorList.get(position));
                                }
                        );
                        spnMajor.setAdapter(spinnerMajorAdapter);
                    }
                })
                .addOnFailureListener(System.out::println);
    }

    private void submitUserData() {
        String fullName = edtFullName.getText().toString();
        String phoneNumber = edtPhoneNumber.getText().toString();
        String workEmail = edtWorkEmail.getText().toString();
        String sex = spnSex.getSelectedItem().toString();
        String major = (String) spnMajor.getSelectedItem();
        String dob = edtDob.getText().toString();
        double yearOfExperience = (Double) spnYearExperience.getSelectedItem();

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        String userUID = firebaseUser.getUid();

        CandidateModel candidateModel = new CandidateModel(
                userUID, firebaseUser.getEmail(), "",
                fullName, phoneNumber, workEmail, sex, dob,
                major, yearOfExperience, System.currentTimeMillis()/1000
        );
        userRepository.insertUser(candidateModel);
    }

    /**
     * Upload ảnh lên Firebase Storage và update database của user
     */
    private void uploadAvatarImage() {
        if (avatarImageBitmap == null) {
            Navigation.findNavController(getActivity(), R.id.navigation_host)
                    .navigate(R.id.action_registerCreateProfileFragment_to_registerVerifyDoneFragment);
            return;
        }

        LoadingScreenDialog loadingScreenDialog = new LoadingScreenDialog(getContext());
        loadingScreenDialog.show();

        StorageReference storageReference = firebaseStorage.getReference("users/avatars");
        StorageReference imageReference = storageReference.child(firebaseAuth.getUid());
        byte[] bytes = ImageHelper.convertBitmapToByteArray(avatarImageBitmap);
        imageReference.putBytes(bytes)
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
