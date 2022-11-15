package com.uet.fwork;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;
import com.uet.fwork.adapter.SpinnerAdapter;
import com.uet.fwork.database.repository.UserRepository;
import com.uet.fwork.util.ImageHelper;
import com.uet.fwork.util.ImagePicker;
//import com.google.firebase.firestore.DocumentReference;
//import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateProfileActivity extends AppCompatActivity {


    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference reference;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private UserRepository userRepository;

    private String currentUid = user.getUid();
    private String name;
    private String email;
    private String phone;
    private String job;
    private String image;
    private String sex;
    private String dateOfBirth;
    private Double yearOfExperience;

    private EditText edtName, edtEmail, edtPhone, edtBirth, edtExp;
    private Button button, btnBack;
    private Spinner spnSex, spnMajor;
    private CircleImageView cirImgAvatar;

    private Bitmap avatarImageBitmap = null;
    private ActivityResultLauncher<Intent> getImageActivityLauncher;
    private Uri avatarImageUri = null;
    private ImageView imgCamera;

    private Calendar calendar;

    private List<String> majorList = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_profile);

        calendar = Calendar.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userRepository = new UserRepository(FirebaseDatabase.getInstance());

        reference = database.getReference("users");

        edtName = findViewById(R.id.nameEdit);
        edtEmail = findViewById(R.id.contactEdit);
        edtPhone = findViewById(R.id.phoneEdit);
        edtBirth = findViewById(R.id.dateOfBirthEdit);
        edtExp = findViewById(R.id.expEdit);
        button = findViewById(R.id.saveEdit);
        spnSex = (Spinner) findViewById(R.id.spnSex);
        spnMajor = (Spinner) findViewById(R.id.spnMajor);
        cirImgAvatar = (CircleImageView) findViewById(R.id.cirImgAvatar);
        imgCamera = (ImageView) findViewById(R.id.imgCamera);
        btnBack = (Button) findViewById(R.id.btnBack);

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

        edtBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(
                        UpdateProfileActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                dateOfBirth = dayOfMonth + "/" + (month + 1) + "/" + year;
                                edtBirth.setText(dateOfBirth);
                            }
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                ).show();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //  Load thông tin cá nhân hiện tại
        Query query = reference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    name = (String) ds.child("fullName").getValue();
                    email = (String) ds.child("email").getValue();
                    phone = (String) ds.child("phoneNumber").getValue();
                    image = (String) ds.child("avatar").getValue();
                    job = (String) ds.child("major").getValue();
                    dateOfBirth = (String) ds.child("dateOfBirth").getValue();
                    sex = (String) ds.child("sex").getValue(String.class);
                    yearOfExperience = ds.child("yearOfExperience").getValue(Double.class);
                    edtName.setText(name);
                    edtEmail.setText(email);
                    edtPhone.setText(phone);
//                    edtJob.setText(job);
                    edtExp.setText(yearOfExperience.toString());
                    edtBirth.setText(dateOfBirth);

                    if (image != null && !image.equals("")) {
                        Picasso.get().load(image).placeholder(R.drawable.wlop_33se).into(cirImgAvatar);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = edtName.getText().toString();
                email = edtEmail.getText().toString();
                phone = edtPhone.getText().toString();
                job = (String) spnMajor.getSelectedItem();
                yearOfExperience = Double.parseDouble(edtExp.getText().toString());
                sex = (String) spnSex.getSelectedItem();

                HashMap result = new HashMap<>();
                result.put("fullName", name);
                result.put("contactEmail", email);
                result.put("phoneNumber", phone);
                result.put("major", job);
                result.put("dateOfBirth",dateOfBirth);
                result.put("sex",sex);
                result.put("yearOfExperience", yearOfExperience);
                reference.child(currentUid).updateChildren(result).addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        //  Back về
                        uploadAvatarImage();
                    }
                });
            }
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
                .get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        GenericTypeIndicator<List<String>> genericTypeIndicator =
                                new GenericTypeIndicator<List<String>>() {};
                        majorList.addAll(dataSnapshot.getValue(genericTypeIndicator));
                        SpinnerAdapter<String> spinnerMajorAdapter = new SpinnerAdapter<>(
                                UpdateProfileActivity.this, majorList, R.layout.item_spinner,
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

    /*
    //Show current user data on editor window
    @Override
    protected void onStart() {
        super.onStart();

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.getResult().exists()) {
                    String nameResult = task.getResult().getString("fullName");
                    String emailResult = task.getResult().getString("contactEmail");
                    String phoneResult = task.getResult().getString("phoneNumber");
                    String jobResult = task.getResult().getString("job");

                    edtName.setText(nameResult);
                    edtEmail.setText(emailResult);
                    edtPhone.setText(phoneResult);
                    edtJob.setText(jobResult);

                }
            },
        });
    }

    private void updateProfile() {
        final String name = edtName.getText().toString();
        final String email = edtEmail.getText().toString();
        final String phone = edtPhone.getText().toString();
        final String job = edtJob.getText().toString();

        final DocumentReference sDoc = db.collection("users").document(currentUid);

        db.runTransaction(new Transaction.Function<Void>() {
                    @Override
                    public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                        // DocumentSnapshot snapshot = transaction.get(sfDocRef);

                        transaction.update(sDoc, "fullName", name);
                        transaction.update(sDoc, "contactEmail", email);
                        transaction.update(sDoc, "job", job);
                        transaction.update(sDoc, "phoneNumber", phone);

                        // Success
                        return null;
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(UpdateProfileFragment.this, "updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UpdateProfileFragment.this, "failed", Toast.LENGTH_SHORT).show();
                    }
                });

    }
    /
     */
}