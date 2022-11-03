package com.uet.fwork;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.firestore.DocumentReference;
//import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class UpdateProfileActivity extends AppCompatActivity {

    EditText edtName, edtEmail, edtPhone, edtJob;
    Button button;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference reference;
//    DocumentReference documentReference;
//    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String currentUid = user.getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_profile);

        reference = database.getReference("users");

        edtName = findViewById(R.id.nameEdit);
        edtEmail = findViewById(R.id.contactEdit);
        edtPhone = findViewById(R.id.phoneEdit);
        edtJob = findViewById(R.id.jobEdit);
        button = findViewById(R.id.saveEdit);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = edtName.getText().toString();
                String email = edtEmail.getText().toString();
                String phone = edtPhone.getText().toString();
                String job = edtJob.getText().toString();

                HashMap result = new HashMap<>();
                result.put("fullName", name);
                result.put("contactEmail", email);
                result.put("phoneNumber", phone);
                result.put("major", job);
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
            }
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