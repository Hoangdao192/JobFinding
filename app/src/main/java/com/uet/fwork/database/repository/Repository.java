package com.uet.fwork.database.repository;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public abstract class Repository {
    protected FirebaseDatabase firebaseDatabase;
    protected DatabaseReference rootDatabaseReference;
    private String referencePathFromRoot = "";

    public Repository(FirebaseDatabase firebaseDatabase) {
        this.firebaseDatabase = firebaseDatabase;
        this.rootDatabaseReference = this.firebaseDatabase.getReference(referencePathFromRoot);
    }

    public Repository(FirebaseDatabase firebaseDatabase, String referencePathFromRoot) {
        this.firebaseDatabase = firebaseDatabase;
        this.referencePathFromRoot = referencePathFromRoot;
        this.rootDatabaseReference = this.firebaseDatabase.getReference(referencePathFromRoot);
    }

    public FirebaseDatabase getFirebaseDatabase() {
        return firebaseDatabase;
    }

    public void setFirebaseDatabase(FirebaseDatabase firebaseDatabase) {
        this.firebaseDatabase = firebaseDatabase;
    }

    public interface OnQuerySuccessListener<T> {
        void onSuccess(T result);
    }
}
