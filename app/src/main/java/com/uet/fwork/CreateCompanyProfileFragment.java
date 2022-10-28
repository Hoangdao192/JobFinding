package com.uet.fwork;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.uet.fwork.adapter.SpinnerAdapter;
import com.uet.fwork.database.model.AddressModel;
import com.uet.fwork.database.model.EmployerModel;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.repository.Repository;
import com.uet.fwork.database.repository.UserRepository;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateCompanyProfileFragment extends Fragment {
    private TextInputLayout edtCompanyName, edtPhoneNumber, edtContactEmail, edtDetailAddress;
    private Spinner spnProvince, spnDistrict, spnWard;
    private Button btnSubmit;
    private final static String API = "https://provinces.open-api.vn/api/";

    private UserRepository userRepository;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private Uri avatarImageUri;

    public CreateCompanyProfileFragment() {
        super(R.layout.fragment_enter_profile_company);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userRepository = new UserRepository(FirebaseDatabase.getInstance());
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtCompanyName = view.findViewById(R.id.edtCompanyName);
        edtContactEmail = view.findViewById(R.id.edtWorkEmail);
        edtDetailAddress = view.findViewById(R.id.edtDetailAddress);
        edtPhoneNumber = view.findViewById(R.id.edtPhoneNumber);
        btnSubmit = view.findViewById(R.id.btnSubmit);

        spnProvince = view.findViewById(R.id.spnProvince);
        spnDistrict = view.findViewById(R.id.spnDistrict);
        spnWard = view.findViewById(R.id.spnWard);

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
                    Navigation.findNavController(getActivity(), R.id.navigation_host)
                            .navigate(R.id.action_registerCreateCompanyProfile_to_registerVerifyDoneFragment);
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
        String province = ((Pair<Integer, String>) spnProvince.getSelectedItem()).second;
        String district = ((Pair<Integer, String>) spnDistrict.getSelectedItem()).second;
        String ward = ((Pair<Integer, String>) spnWard.getSelectedItem()).second;
        userRepository.getUserByUID(userUID, new Repository.OnQuerySuccessListener<UserModel>() {
            @Override
            public void onSuccess(UserModel result) {
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
                userRepository.updateUser(userUID, employerModel);
            }
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
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(API + "?depth=1");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream responseBody = connection.getInputStream();
                        InputStreamReader inputStreamReader = new InputStreamReader(responseBody, "UTF-8");
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        Scanner scanner = new Scanner(responseBody);
                        String response = "";
                        while (scanner.hasNext()) {
                            response += scanner.nextLine();
                        }

                        List<Pair<Integer, String>> provinceMap = new ArrayList<>();
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                            provinceMap.add(Pair.create(jsonObject.getInt("code"), jsonObject.getString("name")));
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                SpinnerAdapter<Pair<Integer, String>> spinnerAdapter = new SpinnerAdapter(
                                        getContext(), provinceMap, R.layout.custom_spinner_2,
                                        new SpinnerAdapter.OnViewCreatedListener() {
                                            @Override
                                            public void onViewCreated(View view, int position) {
                                                TextView txtView = view.findViewById(R.id.txtView);
                                                txtView.setText(provinceMap.get(position).second);
                                            }
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
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void loadDistrict(int provinceCode) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(API + "p/" + provinceCode + "?depth=2");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream responseBody = connection.getInputStream();
                        InputStreamReader inputStreamReader = new InputStreamReader(responseBody, "UTF-8");
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        Scanner scanner = new Scanner(responseBody);
                        String response = "";
                        while (scanner.hasNext()) {
                            response += scanner.nextLine();
                        }

                        List<Pair<Integer, String>> districtMap = new ArrayList<>();
                        JSONObject provinceObject = new JSONObject(response);
                        JSONArray jsonArray = provinceObject.getJSONArray("districts");
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                            districtMap.add(Pair.create(jsonObject.getInt("code"), jsonObject.getString("name")));
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                SpinnerAdapter<Pair<Integer, String>> spinnerAdapter = new SpinnerAdapter(
                                        getContext(), districtMap, R.layout.custom_spinner_2,
                                        new SpinnerAdapter.OnViewCreatedListener() {
                                            @Override
                                            public void onViewCreated(View view, int position) {
                                                TextView txtView = view.findViewById(R.id.txtView);
                                                txtView.setText(districtMap.get(position).second);
                                            }
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
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void loadWard(int districtCode) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(API + "d/" + districtCode + "?depth=2");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream responseBody = connection.getInputStream();
                        InputStreamReader inputStreamReader = new InputStreamReader(responseBody, "UTF-8");
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        Scanner scanner = new Scanner(responseBody);
                        String response = "";
                        while (scanner.hasNext()) {
                            response += scanner.nextLine();
                        }

                        List<Pair<Integer, String>> wardMap = new ArrayList<>();
                        JSONObject districtObject = new JSONObject(response);
                        JSONArray jsonArray = districtObject.getJSONArray("wards");
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                            wardMap.add(Pair.create(jsonObject.getInt("code"), jsonObject.getString("name")));
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                SpinnerAdapter<Pair<Integer, String>> spinnerAdapter = new SpinnerAdapter(
                                        getContext(), wardMap, R.layout.custom_spinner_2,
                                        new SpinnerAdapter.OnViewCreatedListener() {
                                            @Override
                                            public void onViewCreated(View view, int position) {
                                                TextView txtView = view.findViewById(R.id.txtView);
                                                txtView.setText(wardMap.get(position).second);
                                            }
                                        }
                                );
                                spnWard.setAdapter(spinnerAdapter);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
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
