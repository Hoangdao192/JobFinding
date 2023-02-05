package com.vnsoftware.jobfinder.account.profile;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.vnsoftware.jobfinder.dialog.LoadingScreenDialog;
import com.vnsoftware.jobfinder.R;
import com.vnsoftware.jobfinder.adapter.SpinnerAdapter;
import com.vnsoftware.jobfinder.database.model.CandidateModel;
import com.vnsoftware.jobfinder.database.repository.UserRepository;
import com.vnsoftware.jobfinder.util.ImageHelper;
import com.vnsoftware.jobfinder.util.ImagePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateCandidateProfileActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;
    private UserRepository userRepository;

    private EditText edtName, edtEmail, edtPhone, edtBirth, edtExp;
    private Button btnSave, btnBack;
    private Spinner spnSex, spnMajor;
    private CircleImageView cirImgAvatar;

    private Bitmap avatarImageBitmap = null;
    private ActivityResultLauncher<Intent> getImageActivityLauncher;
    private ImageView imgCamera;
    private Calendar calendar;
    private List<String> majorList = new ArrayList<>();

    private CandidateModel candidate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_candidate_profile);

        candidate = (CandidateModel) getIntent().getSerializableExtra("CANDIDATE");

        calendar = Calendar.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userRepository = UserRepository.getInstance();

        edtName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtContactEmail);
        edtPhone = findViewById(R.id.edtPhoneNumber);
        edtBirth = findViewById(R.id.edtDob);
        edtExp = findViewById(R.id.edtExperience);
        btnSave = findViewById(R.id.btnSave);
        spnSex = findViewById(R.id.spnSex);
        spnMajor = findViewById(R.id.spnMajor);
        cirImgAvatar = findViewById(R.id.cirImgAvatar);
        imgCamera = findViewById(R.id.imgCamera);
        btnBack = findViewById(R.id.btnBack);

        loadMajorList();

        //  Khởi tạo dropdown giới tính
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.sex_list, R.layout.custom_spinner
        );
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
        spnSex.setPadding(0, spnSex.getPaddingTop(), spnSex.getPaddingRight(), spnSex.getPaddingBottom());
        spnSex.setAdapter(adapter);

        imgCamera.setOnClickListener(imgCameraView -> {
            Intent intent = ImagePicker.getPickImageIntent(this);
            getImageActivityLauncher.launch(intent);
        });

        //  Date picker cho ngày sinh
        edtBirth.setOnClickListener(editText ->
                new DatePickerDialog(
                UpdateCandidateProfileActivity.this,
                        (datePicker, year, month, dayOfMonth) -> {
                            String dateOfBirth = dayOfMonth + "/" + (month + 1) + "/" + year;
                            edtBirth.setText(dateOfBirth);
                        },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show());

        btnBack.setOnClickListener(button -> finish());

        //  Load dữ liệu hiện tại
        edtName.setText(candidate.getFullName());
        edtEmail.setText(candidate.getEmail());
        edtPhone.setText(candidate.getPhoneNumber());
        edtExp.setText(String.valueOf(candidate.getYearOfExperience()));
        edtBirth.setText(candidate.getDateOfBirth());
        spnSex.setSelection(adapter.getPosition(candidate.getSex()));
        spnMajor.setSelection(majorList.indexOf(candidate.getMajor()));
        String avatarImagePath = candidate.getAvatar();
        if (avatarImagePath != null && !avatarImagePath.equals("")) {
            Picasso.get().load(avatarImagePath).placeholder(R.drawable.wlop_33se).into(cirImgAvatar);
        }

        btnSave.setOnClickListener(button -> {
            candidate.setDateOfBirth(edtBirth.getText().toString());
            candidate.setFullName(edtName.getText().toString());
            candidate.setContactEmail(edtEmail.getText().toString());
            candidate.setPhoneNumber(edtPhone.getText().toString());
            candidate.setMajor((String) spnMajor.getSelectedItem());
            candidate.setYearOfExperience(Double.parseDouble(edtExp.getText().toString()));
            candidate.setSex((String) spnSex.getSelectedItem());
            userRepository.updateUser(candidate, result -> {
                if (result) {
                    uploadAvatarImage();
                }
            });
        });

        this.getImageActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Bitmap bitmap = ImagePicker.getImageFromResult(this, result);
                    if (bitmap != null) {
                        avatarImageBitmap = ImageHelper.reduceImageSize(bitmap, 400, 400);
                        cirImgAvatar.setImageBitmap(avatarImageBitmap);
                    }
                }
        );
    }

    /**
     * Upload ảnh lên Firebase Storage và update database của user
     */
    private void uploadAvatarImage() {
        if (avatarImageBitmap == null) {
            finish();
            return;
        }

        LoadingScreenDialog loadingScreenDialog = new LoadingScreenDialog(this);
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
                        loadingScreenDialog.dismiss();
                        finish();
                    });
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    private void loadMajorList() {
            firebaseDatabase.getReference("userMajors")
                    .get().addOnSuccessListener(dataSnapshot -> {
                        GenericTypeIndicator<List<String>> genericTypeIndicator =
                                new GenericTypeIndicator<List<String>>() {};
                        majorList.addAll(dataSnapshot.getValue(genericTypeIndicator));
                        SpinnerAdapter<String> spinnerMajorAdapter = new SpinnerAdapter<>(
                                UpdateCandidateProfileActivity.this, majorList, R.layout.item_spinner,
                                (itemView, position) -> {
                                    TextView txtView = itemView.findViewById(R.id.txtView);
                                    txtView.setText(majorList.get(position));
                                }
                        );
                        spnMajor.setAdapter(spinnerMajorAdapter);
                    })
                    .addOnFailureListener(System.out::println);
    }
}