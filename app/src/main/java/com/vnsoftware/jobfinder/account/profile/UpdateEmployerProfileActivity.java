package com.vnsoftware.jobfinder.account.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.vnsoftware.jobfinder.dialog.LoadingScreenDialog;
import com.vnsoftware.jobfinder.R;
import com.vnsoftware.jobfinder.adapter.SpinnerAdapter;
import com.vnsoftware.jobfinder.database.model.AddressModel;
import com.vnsoftware.jobfinder.database.model.EmployerModel;
import com.vnsoftware.jobfinder.database.repository.UserRepository;
import com.vnsoftware.jobfinder.util.ImageHelper;
import com.vnsoftware.jobfinder.util.ImagePicker;
import com.vnsoftware.jobfinder.util.VietNameAdministrativeDivisionAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateEmployerProfileActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;
    private UserRepository userRepository;

    private EditText edtName, edtEmail, edtPhone, edtCompanyDescription, edtDetailAddress;
    private Spinner spnProvince, spnDistrict, spnWard;
    private Button btnSave, btnBack;
    private CircleImageView cirImgAvatar;

    private Bitmap avatarImageBitmap = null;
    private ActivityResultLauncher<Intent> getImageActivityLauncher;
    private ImageView imgCamera;

    private EmployerModel employer;

    private VietNameAdministrativeDivisionAPI api;

    private boolean firstDistrictLoad = true, firstWardLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_employer_profile);

        api = VietNameAdministrativeDivisionAPI.getInstance();
        employer = (EmployerModel) getIntent().getSerializableExtra("EMPLOYER");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userRepository = UserRepository.getInstance();

        edtName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtContactEmail);
        edtPhone = findViewById(R.id.edtPhoneNumber);
        edtCompanyDescription = findViewById(R.id.edtDescription);
        edtDetailAddress = findViewById(R.id.edtDetailAddress);
        spnProvince = findViewById(R.id.spnProvince);
        spnDistrict =  findViewById(R.id.spnDistrict);
        spnWard = findViewById(R.id.spnWard);
        loadProvince();

        btnSave = findViewById(R.id.btnSave);
        cirImgAvatar = findViewById(R.id.cirImgAvatar);
        imgCamera = findViewById(R.id.imgCamera);
        btnBack = findViewById(R.id.btnBack);

        imgCamera.setOnClickListener(imgCameraView -> {
            Intent intent = ImagePicker.getPickImageIntent(this);
            getImageActivityLauncher.launch(intent);
        });

        btnBack.setOnClickListener(button -> finish());

        //  Load current data
        edtName.setText(employer.getFullName());
        edtEmail.setText(employer.getEmail());
        edtPhone.setText(employer.getPhoneNumber());
        edtDetailAddress.setText(employer.getAddress().getDetailAddress());
        edtCompanyDescription.setText(employer.getDescription());
        String avatarImagePath = employer.getAvatar();
        if (avatarImagePath != null && !avatarImagePath.equals("")) {
            Picasso.get().load(avatarImagePath).placeholder(R.drawable.wlop_33se).into(cirImgAvatar);
        }

        btnSave.setOnClickListener(button -> {
            updateUserData();
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

    private void updateUserData() {
        employer.setFullName(edtName.getText().toString());
        employer.setContactEmail(edtEmail.getText().toString());
        employer.setPhoneNumber(edtPhone.getText().toString());
        employer.setDescription(edtCompanyDescription.getText().toString());
        String province = ((Pair<Integer,String>)spnProvince.getSelectedItem()).second;
        String district = ((Pair<Integer,String>)spnDistrict.getSelectedItem()).second;
        String ward = ((Pair<Integer,String>)spnWard.getSelectedItem()).second;
        employer.setAddress(new AddressModel(
                province, district, ward,
                edtDetailAddress.getText().toString()
        ));
        userRepository.updateUser(employer, result -> {
            if (result) {
                uploadAvatarImage();
            }
        });
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

    private void loadProvince() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(() -> api.getProvinceList(this, provinceMap -> {
            if (provinceMap != null) {
                handler.post(() -> {
                    SpinnerAdapter<Pair<Integer, String>> spinnerAdapter = new SpinnerAdapter(
                            this, provinceMap, R.layout.custom_spinner_2,
                            (view, position) -> {
                                TextView txtView = view.findViewById(R.id.txtView);
                                txtView.setText(provinceMap.get(position).second);
                            }
                    );
                    spnProvince.setAdapter(spinnerAdapter);
                    int currentProvinceIndex = 0;
                    for (int i = 0; i < provinceMap.size(); ++i) {
                        if (provinceMap.get(i).second.equals(employer.getAddress().getProvince()))
                            currentProvinceIndex = i;
                    }
                    spnProvince.setSelection(currentProvinceIndex);
                    spnProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            loadDistrict(provinceMap.get(position).first);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                });
            }
        }));
    }

    private void loadDistrict(int provinceCode) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(() -> {
            api.getDistrictList(this, provinceCode, districtMap -> {
                SpinnerAdapter<Pair<Integer, String>> spinnerAdapter = new SpinnerAdapter(
                        this, districtMap, R.layout.custom_spinner_2,
                        (view, position) -> {
                            TextView txtView = view.findViewById(R.id.txtView);
                            txtView.setText(districtMap.get(position).second);
                        }
                );
                spnDistrict.setAdapter(spinnerAdapter);
                if (firstDistrictLoad) {
                    int currentDistrictIndex = 0;
                    for (int i = 0; i < districtMap.size(); ++i) {
                        if (districtMap.get(i).second.equals(employer.getAddress().getDistrict()))
                            currentDistrictIndex = i;
                    }
                    spnDistrict.setSelection(currentDistrictIndex);
                    firstDistrictLoad = false;
                }
                spnDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        loadWard(districtMap.get(position).first);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            });
        });
    }

    private void loadWard(int districtCode) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(() -> {
            api.getWardList(this, districtCode, wardMap -> {
                SpinnerAdapter<Pair<Integer, String>> spinnerAdapter = new SpinnerAdapter(
                        this, wardMap, R.layout.custom_spinner_2,
                        (view, position) -> {
                            TextView txtView = view.findViewById(R.id.txtView);
                            txtView.setText(wardMap.get(position).second);
                        }
                );
                spnWard.setAdapter(spinnerAdapter);
                if (firstWardLoad) {
                    int currentWardIndex = 0;
                    for (int i = 0; i < wardMap.size(); ++i) {
                        if (wardMap.get(i).second.equals(employer.getAddress().getWard()))
                            currentWardIndex = i;
                    }
                    spnDistrict.setSelection(currentWardIndex);
                    firstWardLoad = false;
                }
            });

        });
    }
}