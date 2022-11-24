package com.uet.fwork.post;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.uet.fwork.R;
import com.uet.fwork.database.repository.UserRepository;

import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private UserRepository userRepository;
    private DatabaseReference databaseReference;

    private String name, email, uid, dp;

    private EditText edtJobName;
    private EditText edtJobMajor;
    private EditText edtJobAddress;
    private EditText edtJobExperience;
    private EditText edtJobSalary;
    private EditText edtJobDescription;
    private ImageView imgJobImage;
    private Button btnUpload;

    Uri imageUri = null;

    //progress bar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        //display
        edtJobName = findViewById(R.id.job_name);
        edtJobMajor = findViewById(R.id.job_major);
        edtJobAddress = findViewById(R.id.job_address);
        edtJobExperience = findViewById(R.id.job_experience);
        edtJobSalary = findViewById(R.id.job_salary);
        edtJobDescription = findViewById(R.id.job_description);
        imgJobImage = findViewById(R.id.job_image);
        btnUpload = findViewById(R.id.job_upload_button);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        email = user.getEmail();
        uid = user.getUid();

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

        //get image from camera or gallery
        imgJobImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialogue();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get data from Edit Texts
                String jobName = edtJobName.getText().toString().trim();
                String jobMajor = edtJobMajor.getText().toString().trim();
                String jobAddress = edtJobAddress.getText().toString().trim();
                String jobExperience = edtJobExperience.getText().toString().trim();
                String jobSalary = edtJobSalary.getText().toString().trim();
                String jobDescription = edtJobDescription.getText().toString().trim();

                if (TextUtils.isEmpty(jobName)) {
                    Toast.makeText(AddPostActivity.this, "Bạn chưa nhập tên công việc!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(jobMajor)) {
                    Toast.makeText(AddPostActivity.this, "Bạn chưa nhập chuyên ngành công việc!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(jobAddress)) {
                    Toast.makeText(AddPostActivity.this, "Bạn chưa nhập địa chỉ công việc!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(jobExperience)) {
                    Toast.makeText(AddPostActivity.this, "Bạn chưa nhập kinh nghiệm yêu cầu!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(jobSalary)) {
                    Toast.makeText(AddPostActivity.this, "Bạn chưa nhập lương công việc!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(jobDescription)) {
                    Toast.makeText(AddPostActivity.this, "Bạn chưa nhập mô tả công việc!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (imageUri == null) {
                    //post without image
                    uploadPost(jobName, jobMajor, jobAddress, jobExperience, jobSalary, jobDescription, "noImage");
                } else {
                    //post with image
                    uploadPost(jobName, jobMajor, jobAddress, jobExperience, jobSalary, jobDescription, String.valueOf(imageUri));
                }
            }
        });

    }

    private void showImagePickDialogue() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose image from:");
        //set items
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }
                }
                if (i == 1) {
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                }
            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        cv.put(MediaStore.Images.Media.TITLE, "Temp Descr");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, CAMERA_REQUEST_CODE);
    }

    private void uploadPost(String j_name, String j_major, String j_address, String j_exp, String j_salary, String j_description, String uri) {
        String timeStamp = String.valueOf(System.currentTimeMillis());

        String filePathAndName = "posts/" + "post_" + timeStamp;

        if (!uri.equals("noImage")) {
            //post with image
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putFile(Uri.parse(uri)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Image is uploaded to firebase storage
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;
                    String downloadUri = uriTask.getResult().toString();
                    if (uriTask.isSuccessful()) {
                        //url is received upload post to firebase db

                        HashMap<Object, String> hashMap = new HashMap<>();
                        hashMap.put("uid", uid);
                        hashMap.put("userName", name);
                        hashMap.put("userEmail", email);
                        hashMap.put("userDp", dp);
                        hashMap.put("postId", timeStamp);
                        hashMap.put("postName", j_name);
                        hashMap.put("postMajor", j_major);
                        hashMap.put("postAddress", j_address);
                        hashMap.put("postExperience", j_exp);
                        hashMap.put("postSalary", j_salary);
                        hashMap.put("postDescription", j_description);
                        hashMap.put("postImage", downloadUri);

                        //path to storage
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("posts");
                        //store data
                        databaseReference.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                //post added
                                Toast.makeText(AddPostActivity.this, "Bài viết đã được đăng", Toast.LENGTH_SHORT).show();
                                //reset views
                                edtJobName.setText("");
                                edtJobAddress.setText("");
                                edtJobDescription.setText("");
                                edtJobExperience.setText("");
                                edtJobMajor.setText("");
                                edtJobSalary.setText("");
                                imgJobImage.setImageURI(null);
                                imageUri = null;
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //failed to add post
                                Toast.makeText(AddPostActivity.this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //failed to upload image
                    Toast.makeText(AddPostActivity.this, "Ảnh bị lỗi", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            //post without image
            HashMap<Object, String> hashMap = new HashMap<>();
            hashMap.put("uid", uid);
            hashMap.put("userName", name);
            hashMap.put("userEmail", email);
            hashMap.put("userDp", dp);
            hashMap.put("postId", timeStamp);
            hashMap.put("postName", j_name);
            hashMap.put("postMajor", j_major);
            hashMap.put("postAddress", j_address);
            hashMap.put("postExperience", j_exp);
            hashMap.put("postSalary", j_salary);
            hashMap.put("postDescription", j_description);
            hashMap.put("postImage", "noImage");

            //path to storage
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("posts");
            //store data
            databaseReference.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    //post added
                    Toast.makeText(AddPostActivity.this, "Bài viết đã được đăng", Toast.LENGTH_SHORT).show();
                    edtJobName.setText("");
                    edtJobAddress.setText("");
                    edtJobDescription.setText("");
                    edtJobExperience.setText("");
                    edtJobMajor.setText("");
                    edtJobSalary.setText("");
                    imgJobImage.setImageURI(null);
                    imageUri = null;

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //failed to add post
                    Toast.makeText(AddPostActivity.this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
}