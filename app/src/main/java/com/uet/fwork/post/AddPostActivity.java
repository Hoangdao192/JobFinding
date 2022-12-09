package com.uet.fwork.post;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.uet.fwork.R;
import com.uet.fwork.account.profile.UpdateCandidateProfileActivity;
import com.uet.fwork.adapter.SpinnerAdapter;
import com.uet.fwork.database.model.post.PostModel;
import com.uet.fwork.database.repository.PostRepository;
import com.uet.fwork.dialog.ErrorDialog;
import com.uet.fwork.dialog.LoadingScreenDialog;
import com.uet.fwork.map.SearchPlaceActivity;
import com.uet.fwork.util.ApiAddress;
import com.uet.fwork.util.ImageHelper;
import com.uet.fwork.util.ImagePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddPostActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    private PostRepository postRepository;
    private FirebaseDatabase firebaseDatabase;

    private String name, email, uid, dp;
    private double jobAddressLatitude = -1d, jobAddressLongitude = -1d;
    private List<String> majorList = new ArrayList<>();

    private EditText edtJobName;
    private Spinner spnJobMajor;
    private EditText edtJobAddress;
    private EditText edtJobExperience;
    private EditText edtJobSalary;
    private EditText edtJobDescription;
    private ImageView imgJobImage;
    private ImageView btnPickOnMap;
    private Button btnUpload, btnBack;

    private Bitmap postImage = null;

    private ActivityResultLauncher<Intent> getImageActivityLauncher;
    private ActivityResultLauncher<Intent> getLocationFromMapActivityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        postRepository = PostRepository.getInstance();

        //display
        btnPickOnMap = findViewById(R.id.btnPickOnMap);
        edtJobName = findViewById(R.id.edtJobName);
        spnJobMajor = findViewById(R.id.spnJobMajor);
        edtJobAddress = findViewById(R.id.edtJobAddress);
        edtJobExperience = findViewById(R.id.edtJobExperience);
        edtJobSalary = findViewById(R.id.edtJobSalary);
        edtJobDescription = findViewById(R.id.edtJobDescription);
        imgJobImage = findViewById(R.id.ivJobImage);
        btnUpload = findViewById(R.id.btnPostUpload);
        btnBack = findViewById(R.id.btnBack);

        firebaseDatabase = FirebaseDatabase.getInstance();

        loadMajorList();
        btnBack.setOnClickListener(button -> finish());

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        email = user.getEmail();
        uid = user.getUid();

        this.getLocationFromMapActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        ApiAddress apiAddress = (ApiAddress) result.getData().getSerializableExtra("address");
                        edtJobAddress.setText(apiAddress.getFullAddress());
                        if (apiAddress.getLatitude() != -1d) {
                            jobAddressLatitude = apiAddress.getLatitude();
                            jobAddressLongitude = apiAddress.getLongitude();
                        }
                    }
//                    System.out.println(result.getData().getSerializableExtra("address").toString());
                }
        );

        btnPickOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddPostActivity.this, SearchPlaceActivity.class);
                getLocationFromMapActivityLauncher.launch(intent);
            }
        });

        //show user info on post
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        Query query = databaseReference.orderByChild("id").equalTo(user.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    name = "" + ds.child("fullName").getValue();
                    email = "" + ds.child("email").getValue();
                    dp = "" + ds.child("avatar").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        this.getImageActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Bitmap bitmap = ImagePicker.getImageFromResult(this, result);
                    if (bitmap != null) {
                        postImage = ImageHelper.reduceImageSize(bitmap, 1000, 1000);
                        imgJobImage.setImageBitmap(postImage);
                    }
                }
        );

        //get image from camera or gallery
        imgJobImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = ImagePicker.getPickImageIntent(AddPostActivity.this);
                getImageActivityLauncher.launch(intent);
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (edtJobExperience.getText().toString().trim().length() == 0) {
                    Toast.makeText(AddPostActivity.this, "Bạn chưa nhập kinh nghiệm yêu cầu!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (edtJobSalary.getText().toString().trim().length() == 0) {
                    Toast.makeText(AddPostActivity.this, "Bạn chưa nhập lương công việc!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (edtJobName.getText().toString().trim().length() == 0) {
                    Toast.makeText(AddPostActivity.this, "Bạn chưa nhập tên công việc!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (spnJobMajor.getSelectedItem().toString().trim().length() == 0) {
                    Toast.makeText(AddPostActivity.this, "Bạn chưa chọn chuyên ngành công việc!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (edtJobAddress.getText().toString().trim().length() == 0) {
                    Toast.makeText(AddPostActivity.this, "Bạn chưa nhập địa chỉ công việc!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (edtJobDescription.getText().toString().trim().length() == 0) {
                    Toast.makeText(AddPostActivity.this, "Bạn chưa nhập mô tả công việc!", Toast.LENGTH_SHORT).show();
                    return;
                }

                //get data from Edit Texts
                String jobName = edtJobName.getText().toString().trim();
                String jobMajor = spnJobMajor.getSelectedItem().toString().trim();
                String jobAddress = edtJobAddress.getText().toString().trim();
                double jobExperience = Double.parseDouble(edtJobExperience.getText().toString().trim());
                Long jobSalary = Long.valueOf(edtJobSalary.getText().toString().trim());
                String jobDescription = edtJobDescription.getText().toString().trim();

                uploadPost(jobName, jobMajor, jobAddress, jobExperience, jobSalary, jobDescription);
            }
        });

    }

    private void uploadPost(
            String jobName, String jobMajor, String jobAddress,
            double jobExperience, Long jobSalary, String jobDescription) {
        //String timeStamp = String.valueOf(System.currentTimeMillis());
        Long timeStamp = Calendar.getInstance().getTimeInMillis() / 1000;
        String filePathAndName = "posts/" + "post_" + timeStamp;

        LoadingScreenDialog loadingDialog = new LoadingScreenDialog(this);
        loadingDialog.show();
        if (postImage != null) {
            //post with image
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(ImageHelper.convertBitmapToByteArray(postImage))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //Image is uploaded to firebase storage
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;
                            String downloadUri = uriTask.getResult().toString();
                            if (uriTask.isSuccessful()) {
                                //url is received upload post to firebase db

                                PostModel postModel = new PostModel(
                                        jobName, jobMajor, jobAddress, jobExperience,
                                        jobSalary, jobDescription, timeStamp, downloadUri.toString(),
                                        uid, name, email, dp
                                );
                                postModel.setLatitude(jobAddressLatitude);
                                postModel.setLongitude(jobAddressLongitude);

                                postRepository.insert(postModel, success -> {
                                    loadingDialog.dismiss();
                                    if (success) {
                                        //post added
                                        ErrorDialog dialog = new ErrorDialog(
                                                AddPostActivity.this, "Đăng bài thành công",
                                                "Bài viết đã được đăng");
                                        //reset views

                                        edtJobName.setText("");
                                        edtJobAddress.setText("");
                                        edtJobDescription.setText("");
                                        edtJobExperience.setText("");
                                        //edtJobMajor.setText("");
                                        edtJobSalary.setText("");
                                        imgJobImage.setImageBitmap(null);
                                    } else {
                                        ErrorDialog dialog = new ErrorDialog(
                                                AddPostActivity.this, "Đăng bài không thành công",
                                                "Có lỗi xảy ra");
                                        dialog.show();
                                    }
                                });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed to upload image
                            ErrorDialog dialog = new ErrorDialog(
                                    AddPostActivity.this, "Đăng bài không thành công",
                                    "Không thể upload ảnh");
                            dialog.show();
                        }
                    });
        } else {
            //post without image
            PostModel postModel = new PostModel(
                    jobName, jobMajor, jobAddress, jobExperience,
                    jobSalary, jobDescription, timeStamp, "",
                    uid, name, email, dp
            );
            postModel.setLatitude(jobAddressLatitude);
            postModel.setLongitude(jobAddressLongitude);

            postRepository.insert(postModel, success -> {
                loadingDialog.dismiss();
                ErrorDialog dialog;
                if (success) {
                    //post added
                    dialog = new ErrorDialog(
                            AddPostActivity.this, "Đăng bài thành công",
                            "Bài viết đã được đăng");
                    //reset views
                    edtJobName.setText("");
                    edtJobAddress.setText("");
                    edtJobDescription.setText("");
                    edtJobExperience.setText("");
                    //edtJobMajor.setText("");
                    edtJobSalary.setText("");
                    imgJobImage.setImageBitmap(null);
                } else {
                    dialog = new ErrorDialog(
                            AddPostActivity.this, "Đăng bài không thành công",
                            "Có lỗi xảy ra");
                }
                dialog.show();
            });
        }

    }

    private void loadMajorList() {
        firebaseDatabase.getReference("userMajors")
                .get().addOnSuccessListener(dataSnapshot -> {
                    GenericTypeIndicator<List<String>> genericTypeIndicator =
                            new GenericTypeIndicator<List<String>>() {};
                    majorList.addAll(dataSnapshot.getValue(genericTypeIndicator));
                    SpinnerAdapter<String> spinnerMajorAdapter = new SpinnerAdapter<>(
                            AddPostActivity.this, majorList, R.layout.item_spinner,
                            (itemView, position) -> {
                                TextView txtView = itemView.findViewById(R.id.txtView);
                                txtView.setText(majorList.get(position));
                            }
                    );
                    spnJobMajor.setAdapter(spinnerMajorAdapter);
                })
                .addOnFailureListener(System.out::println);
    }
}