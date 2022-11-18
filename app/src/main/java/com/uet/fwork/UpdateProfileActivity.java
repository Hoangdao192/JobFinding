package com.uet.fwork;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.firestore.DocumentReference;
//import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.logging.Logger;

public class UpdateProfileActivity extends AppCompatActivity {

    EditText edtName, edtEmail, edtPhone, edtJob, edtBirth, edtExp;
    RadioButton radioMale, radioFemale;
    Button button;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference reference;
    RadioGroup radioGroup;
//    DocumentReference documentReference;
//    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String currentUid = user.getUid();
    String name;
    String email;
    String phone;
    String job;
    String image;
    String sex;
    String dateOfBirth;
    String expeYears;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_profile);

        reference = database.getReference("users");

        edtName = findViewById(R.id.nameEdit);
        edtEmail = findViewById(R.id.contactEdit);
        edtPhone = findViewById(R.id.phoneEdit);
        edtJob = findViewById(R.id.jobEdit);
        edtBirth = findViewById(R.id.dateOfBirthEdit);
        edtExp = findViewById(R.id.expEdit);
        radioMale = (RadioButton) findViewById(R.id.radioMale);
        radioFemale = (RadioButton) findViewById(R.id.radioFemale);
        radioGroup = findViewById(R.id.radioGroup);
        button = findViewById(R.id.saveEdit);

        Query query = reference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    name = ""+ ds.child("fullName").getValue().toString();
                    email = ""+ ds.child("email").getValue().toString();
                    phone = ""+ ds.child("phoneNumber").getValue().toString();
                    image = ""+ ds.child("avatar").getValue().toString();
                    job = "" + ds.child("major").getValue().toString();
                    dateOfBirth = "" + ds.child("dateOfBirth").getValue().toString();
                    sex = "" +ds.child("sex").getValue();
                    expeYears = "" + ds.child("yearOfExperience").getValue();

                    edtName.setText(name);
                    edtEmail.setText(email);
                    edtPhone.setText(phone);
                    edtJob.setText(job);
                    edtExp.setText(expeYears);
                    edtBirth.setText(dateOfBirth);
                    if (sex.equals("Nữ")) {
                        radioFemale.toggle();
                    } else {
                        radioMale.toggle();
                    }
                    //Picasso.get().load(image).into(avatarIv);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.radioMale:
                        sex = "Nam";
                        System.out.println(sex);
                    break;
                    case R.id.radioFemale: sex = "Nữ";
                        System.out.println(sex);
                    break;
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = edtName.getText().toString();
                email = edtEmail.getText().toString();
                phone = edtPhone.getText().toString();
                job = edtJob.getText().toString();
                expeYears = edtExp.getText().toString();
                dateOfBirth = edtBirth.getText().toString();


                HashMap result = new HashMap<>();
                result.put("fullName", name);
                result.put("contactEmail", email);
                result.put("phoneNumber", phone);
                result.put("major", job);
                result.put("dateOfBirth",dateOfBirth);
                result.put("sex",sex);
                result.put("yearOfExperience", Double.parseDouble(expeYears));
                reference.child(currentUid).updateChildren(result);
            }
        });
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