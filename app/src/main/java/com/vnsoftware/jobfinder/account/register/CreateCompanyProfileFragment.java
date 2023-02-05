package com.vnsoftware.jobfinder.account.register;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
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

public class CreateCompanyProfileFragment extends Fragment {
    private TextInputLayout edtCompanyName, edtPhoneNumber, edtContactEmail, edtDetailAddress;
    private Spinner spnProvince, spnDistrict, spnWard;
    private Button btnSubmit;
    private ImageView imgCamera;
    private CircleImageView cirImgAvatar;
    private final static String API = "https://provinces.open-api.vn/api/";
    private VietNameAdministrativeDivisionAPI api;

    private UserRepository userRepository;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private Bitmap avatarImageBitmap;

    private ActivityResultLauncher<Intent> getImageActivityLauncher;
    private Uri avatarImageUri;

    public CreateCompanyProfileFragment() {
        super(R.layout.fragment_enter_profile_company);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userRepository = UserRepository.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        api = VietNameAdministrativeDivisionAPI.getInstance();

        this.getImageActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Bitmap bitmap = ImagePicker.getImageFromResult(getContext(), result);
                        if (bitmap != null) {
                            avatarImageBitmap = ImageHelper.reduceImageSize(bitmap, 400, 400);
                            cirImgAvatar.setImageBitmap(avatarImageBitmap);
                        }
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtCompanyName = view.findViewById(R.id.edtCompanyName);
        edtContactEmail = view.findViewById(R.id.edtWorkEmail);
        edtDetailAddress = view.findViewById(R.id.edtDetailAddress);
        edtPhoneNumber = view.findViewById(R.id.edtPhoneNumber);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        imgCamera = view.findViewById(R.id.imgCamera);
        cirImgAvatar = view.findViewById(R.id.cirImgAvatar);

        spnProvince = view.findViewById(R.id.spnProvince);
        spnDistrict = view.findViewById(R.id.spnDistrict);
        spnWard = view.findViewById(R.id.spnWard);

        imgCamera.setOnClickListener(imgCameraView -> {
            Intent intent = ImagePicker.getPickImageIntent(getContext());
            getImageActivityLauncher.launch(intent);
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearTextInputLayoutError();
                if (
                    checkingAndNotifyBlankInput(edtCompanyName)
                    && checkingAndNotifyBlankInput(edtPhoneNumber)
                    && checkingAndNotifyBlankInput(edtContactEmail)
                ) {
                    submitUserData();
                    uploadAvatarImage();
                }
            }
        });

        loadProvince();
    }

    private void submitUserData() {
        String userUID = firebaseAuth.getCurrentUser().getUid();
        String companyName = edtCompanyName.getEditText().getText().toString();
        String contactEmail = edtContactEmail.getEditText().getText().toString();
        String phoneNumber = edtPhoneNumber.getEditText().getText().toString();
        String detailAddress = edtDetailAddress.getEditText().getText().toString();
        String province = ((Pair<Integer,String>)spnProvince.getSelectedItem()).second;
        String district = ((Pair<Integer,String>)spnDistrict.getSelectedItem()).second;
        String ward = ((Pair<Integer,String>)spnWard.getSelectedItem()).second;
        userRepository.getUserByUID(userUID, result -> {
            System.out.println(result.toString());
            EmployerModel employerModel = (EmployerModel) result;
            employerModel.setFullName(companyName);
            employerModel.setContactEmail(contactEmail);
            employerModel.setPhoneNumber(phoneNumber);
            employerModel.setAddress(
                    new AddressModel(
                            province, district, ward,
                            detailAddress
                    )
            );
            employerModel.setLastUpdate(System.currentTimeMillis() / 1000);
            userRepository.updateUser(userUID, employerModel);
        });
    }


    private void clearTextInputLayoutError() {
        edtCompanyName.setErrorEnabled(false);
        edtContactEmail.setErrorEnabled(false);
        edtDetailAddress.setErrorEnabled(false);
        edtPhoneNumber.setErrorEnabled(false);
    }

    /**
     * Kiểm tra và đưa ra thông báo khi inputLayout chưa được nhập
     * @param inputLayout
     * @return
     *  false : inputLayout trống
     *  true : inputLayout có dữ liệu
     */
    private boolean checkingAndNotifyBlankInput(TextInputLayout inputLayout) {
        if (inputLayout.getEditText().getText().toString().isEmpty()) {
            inputLayout.setError("Bạn chưa nhập trường này.");
            return false;
        }
        return true;
    }

    private void loadProvince() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(() -> api.getProvinceList(getContext(), provinceMap -> {
            if (provinceMap != null) {
                handler.post(() -> {
                    SpinnerAdapter<Pair<Integer, String>> spinnerAdapter = new SpinnerAdapter(
                            getContext(), provinceMap, R.layout.custom_spinner_2,
                            (view, position) -> {
                                TextView txtView = view.findViewById(R.id.txtView);
                                txtView.setText(provinceMap.get(position).second);
                            }
                    );
                    spnProvince.setAdapter(spinnerAdapter);
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
            api.getDistrictList(getContext(), provinceCode, districtMap -> {
                SpinnerAdapter<Pair<Integer, String>> spinnerAdapter = new SpinnerAdapter(
                        getContext(), districtMap, R.layout.custom_spinner_2,
                        (view, position) -> {
                            TextView txtView = view.findViewById(R.id.txtView);
                            txtView.setText(districtMap.get(position).second);
                        }
                );
                spnDistrict.setAdapter(spinnerAdapter);
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
            api.getWardList(getContext(), districtCode, wardMap -> {
                SpinnerAdapter<Pair<Integer, String>> spinnerAdapter = new SpinnerAdapter(
                        getContext(), wardMap, R.layout.custom_spinner_2,
                        (view, position) -> {
                            TextView txtView = view.findViewById(R.id.txtView);
                            txtView.setText(wardMap.get(position).second);
                        }
                );
                spnWard.setAdapter(spinnerAdapter);
            });

        });
    }

    /**
     * Upload ảnh lên Firebase Storage và update database của user
     */
    private void uploadAvatarImage() {
        if (avatarImageBitmap == null) {
            Navigation.findNavController(getActivity(), R.id.navigation_host)
                    .navigate(R.id.action_createCompanyProfileFragment_to_registerVerifyDoneFragment);
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
                        Navigation.findNavController(getActivity(), R.id.navigation_host)
                                .navigate(R.id.action_createCompanyProfileFragment_to_registerVerifyDoneFragment);
                    });
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }
}
