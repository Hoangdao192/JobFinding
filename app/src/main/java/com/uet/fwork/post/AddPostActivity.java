package com.uet.fwork.post;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.uet.fwork.R;
import com.uet.fwork.adapter.SpinnerAdapter;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.model.post.PostModel;
import com.uet.fwork.database.repository.PostRepository;
import com.uet.fwork.database.repository.Repository;
import com.uet.fwork.dialog.ErrorDialog;
import com.uet.fwork.dialog.LoadingScreenDialog;
import com.uet.fwork.firebasehelper.FirebaseAuthHelper;
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
    private PostRepository postRepository;
    private FirebaseDatabase firebaseDatabase;

    private String name, email, uid, dp;
    private double jobAddressLatitude = -1d, jobAddressLongitude = -1d;
    private List<String> majorList = new ArrayList<>();
    private List<Bitmap> uploadBitmapList = new ArrayList<>();

    private EditText edtJobName;
    private EditText edtMajor;
    private EditText edtJobAddress;
    private EditText edtJobExperience;
    private EditText edtJobSalary;
    private EditText edtJobDescription;
    private Button btnAddImage;
    private Button btnPickOnMap;
    private Button btnUpload, btnBack;
    private RecyclerView recUploadImage;

    private Bitmap postImage = null;

    private ActivityResultLauncher<Intent> getImageActivityLauncher;
    private ActivityResultLauncher<Intent> getLocationFromMapActivityLauncher;

    private UserModel currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        postRepository = PostRepository.getInstance();

        //display
        btnPickOnMap = findViewById(R.id.btnPickOnMap);
        edtJobName = findViewById(R.id.edtJobName);
        edtMajor = findViewById(R.id.edtMajor);
        edtJobAddress = findViewById(R.id.edtAddress);
        edtJobExperience = findViewById(R.id.edtExperience);
        edtJobSalary = findViewById(R.id.edtSalary);
        edtJobDescription = findViewById(R.id.edtJobDescription);
        btnAddImage = findViewById(R.id.btnAddImage);
        btnUpload = findViewById(R.id.btnPostUpload);
        btnBack = findViewById(R.id.btnBack);
        recUploadImage = (RecyclerView) findViewById(R.id.recUploadImage);

        firebaseDatabase = FirebaseDatabase.getInstance();

        btnBack.setOnClickListener(button -> finish());

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        email = user.getEmail();
        uid = user.getUid();

        currentUser = FirebaseAuthHelper.getInstance().getUser();

        //  Initialize pick location on map
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
                }
        );

        //  Initialize upload image recyclerview
        PostImageRecyclerViewAdapter adapter = new PostImageRecyclerViewAdapter(
                uploadBitmapList, this
        );
        DividerItemDecoration horizontalDivider = new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL);
        horizontalDivider.setDrawable(AppCompatResources.getDrawable(this, R.drawable.divider));
        recUploadImage.addItemDecoration(horizontalDivider);
        DividerItemDecoration verticalDivider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        verticalDivider.setDrawable(AppCompatResources.getDrawable(this, R.drawable.divider));
        recUploadImage.addItemDecoration(verticalDivider);
        recUploadImage.setAdapter(adapter);
        recUploadImage.setLayoutManager(new FlexboxLayoutManager(this));

        btnPickOnMap.setOnClickListener(v -> {
            Intent intent = new Intent(AddPostActivity.this, SearchPlaceActivity.class);
            getLocationFromMapActivityLauncher.launch(intent);
        });

//        //show user info on post
//        databaseReference = FirebaseDatabase.getInstance().getReference("users");
//        Query query = databaseReference.orderByChild("id").equalTo(user.getUid());
//        query.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot ds : snapshot.getChildren()) {
//                    name = "" + ds.child("fullName").getValue();
//                    email = "" + ds.child("email").getValue();
//                    dp = "" + ds.child("avatar").getValue();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//            }
//        });

        this.getImageActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Bitmap bitmap = ImagePicker.getImageFromResult(this, result);
                    if (bitmap != null) {
                        postImage = ImageHelper.reduceImageSize(bitmap, 1000, 1000);
                        //  TODO: Handle image upload
                        uploadBitmapList.add(postImage);
                        adapter.notifyItemInserted(uploadBitmapList.size());
//                        btnAddImage.setImageBitmap(postImage);
                    }
                }
        );

        //get image from camera or gallery
        btnAddImage.setOnClickListener(btn -> {
            Intent intent = ImagePicker.getPickImageIntent(AddPostActivity.this);
            getImageActivityLauncher.launch(intent);
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
                if (edtMajor.getText().toString().trim().length() == 0) {
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
                String jobMajor = edtMajor.getText().toString();
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
        Long timeStamp = Calendar.getInstance().getTimeInMillis() / 1000;

        LoadingScreenDialog loadingDialog = new LoadingScreenDialog(this);
        loadingDialog.show();

        PostModel postModel = new PostModel(
                jobName, jobMajor, jobAddress, jobExperience,
                jobSalary, jobDescription, timeStamp, "",
                currentUser.getId(), currentUser.getFullName(), currentUser.getEmail(),
                currentUser.getAvatar()
        );
        postModel.setLatitude(jobAddressLatitude);
        postModel.setLongitude(jobAddressLongitude);

        DatabaseReference reference = postRepository.getRootDatabaseReference().push();

        Repository.OnQuerySuccessListener<Boolean> uploadPostListener = success -> {
            loadingDialog.dismiss();
            if (success) {
                //post added
                ErrorDialog dialog = new ErrorDialog(
                        AddPostActivity.this, "Đăng bài thành công",
                        "Bài viết đã được đăng");
                // TODO: Reset views
            } else {
                ErrorDialog dialog = new ErrorDialog(
                        AddPostActivity.this, "Đăng bài không thành công",
                        "Có lỗi xảy ra");
                dialog.show();
            }
        };

        if (uploadBitmapList.size() != 0) {
            //post with image
            String filePathAndName = "posts/" + reference.getKey();
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(ImageHelper.convertBitmapToByteArray(uploadBitmapList.get(0)))
                    .addOnSuccessListener(taskSnapshot -> {
                        //Image is uploaded to firebase storage
                        taskSnapshot.getStorage().getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    postModel.setPostImage(uri.getPath());
                                    postRepository.insert(reference, postModel, uploadPostListener);
                                });
                    })
                    .addOnFailureListener(e -> {
                        //  Failed to upload image
                        ErrorDialog dialog = new ErrorDialog(
                                AddPostActivity.this, "Đăng bài không thành công",
                                "Không thể upload ảnh");
                        dialog.show();
                    });
        } else {
            postRepository.insert(reference, postModel, uploadPostListener);
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
//                    spnJobMajor.setAdapter(spinnerMajorAdapter);
                })
                .addOnFailureListener(System.out::println);
    }
}