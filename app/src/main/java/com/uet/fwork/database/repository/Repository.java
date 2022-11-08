package com.uet.fwork.database.repository;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public abstract class Repository {
    protected FirebaseDatabase firebaseDatabase;
    protected FirebaseFirestore firebaseFirestore;
    protected DatabaseReference rootDatabaseReference;
    protected CollectionReference rootCollectionReference;
    protected String referencePathFromRoot = "";

    public Repository(FirebaseDatabase firebaseDatabase) {
        this.firebaseDatabase = firebaseDatabase;
        this.rootDatabaseReference = this.firebaseDatabase.getReference(referencePathFromRoot);
    }

    public Repository(FirebaseDatabase firebaseDatabase, FirebaseFirestore firebaseFirestore, String referencePathFromRoot) {
        this.referencePathFromRoot = referencePathFromRoot;
        this.firebaseDatabase = firebaseDatabase;
        this.firebaseFirestore = firebaseFirestore;
        this.rootDatabaseReference = this.firebaseDatabase.getReference(referencePathFromRoot);
        this.rootCollectionReference = this.firebaseFirestore.collection(referencePathFromRoot);
    }

    public Repository(FirebaseDatabase firebaseDatabase, String referencePathFromRoot) {
        this.referencePathFromRoot = referencePathFromRoot;
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

    public void setFirebaseFirestore(FirebaseFirestore firebaseFirestore) {
        this.firebaseFirestore = firebaseFirestore;
    }

    public DatabaseReference getRootDatabaseReference() {
        return rootDatabaseReference;
    }

    public interface OnQuerySuccessListener<T> {
        void onSuccess(T result);
    }
}
