package com.uet.fwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.widget.ProgressBar;
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
import com.uet.fwork.database.repository.UserRepository;

import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private UserRepository userRepository;
    private DatabaseReference databaseReference;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    String[] cameraPermissions;
    String[] storagePermissions;

    String name, email, uid, dp;

    EditText jobName, jobMajor, jobAddress, jobExperience, jobSalary, jobDescription;
    ImageView jobImage;
    Button jobUploadBtn;

    Uri image_rui = null;

    //progress bar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        //init permissions arrays
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //display
        jobName = findViewById(R.id.job_name);
        jobMajor = findViewById(R.id.job_major);
        jobAddress = findViewById(R.id.job_address);
        jobExperience = findViewById(R.id.job_experience);
        jobSalary = findViewById(R.id.job_salary);
        jobDescription = findViewById(R.id.job_description);
        jobImage = findViewById(R.id.job_image);
        jobUploadBtn = findViewById(R.id.job_upload_button);

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
        jobImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialogue();
            }
        });

        jobUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get data from Edit Texts
                String j_name = jobName.getText().toString().trim();
                String j_major = jobMajor.getText().toString().trim();
                String j_address = jobAddress.getText().toString().trim();
                String j_exp = jobExperience.getText().toString().trim();
                String j_salary = jobSalary.getText().toString().trim();
                String j_description = jobDescription.getText().toString().trim();

                if (TextUtils.isEmpty(j_name)) {
                    Toast.makeText(AddPostActivity.this, "Bạn chưa nhập tên công việc!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(j_major)) {
                    Toast.makeText(AddPostActivity.this, "Bạn chưa nhập chuyên ngành công việc!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(j_address)) {
                    Toast.makeText(AddPostActivity.this, "Bạn chưa nhập địa chỉ công việc!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(j_exp)) {
                    Toast.makeText(AddPostActivity.this, "Bạn chưa nhập kinh nghiệm yêu cầu!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(j_salary)) {
                    Toast.makeText(AddPostActivity.this, "Bạn chưa nhập lương công việc!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(j_description)) {
                    Toast.makeText(AddPostActivity.this, "Bạn chưa nhập mô tả công việc!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (image_rui == null) {
                    //post without image
                    uploadPost(j_name, j_major, j_address, j_exp, j_salary, j_description, "noImage");
                } else {
                    //post with image
                    uploadPost(j_name, j_major, j_address, j_exp, j_salary, j_description, String.valueOf(image_rui));
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
        image_rui = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_rui);
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
                                jobName.setText("");
                                jobAddress.setText("");
                                jobDescription.setText("");
                                jobExperience.setText("");
                                jobMajor.setText("");
                                jobSalary.setText("");
                                jobImage.setImageURI(null);
                                image_rui = null;
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
                    jobName.setText("");
                    jobAddress.setText("");
                    jobDescription.setText("");
                    jobExperience.setText("");
                    jobMajor.setText("");
                    jobSalary.setText("");
                    jobImage.setImageURI(null);
                    image_rui = null;

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

    //handle permission results


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(this, "Cần quyền truy cập Camera và bộ nhớ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Cần quyền truy cập kho ảnh", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                image_rui = data.getData();
                jobImage.setImageURI(image_rui);

            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                jobImage.setImageURI(image_rui);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}