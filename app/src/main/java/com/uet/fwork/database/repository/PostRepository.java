package com.uet.fwork.database.repository;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class PostRepository extends Repository {
    private static final String REFERENCE_PATH = "post";

    public PostRepository(FirebaseDatabase firebaseDatabase, FirebaseFirestore firebaseFirestore) {
        super(firebaseDatabase, firebaseFirestore, REFERENCE_PATH);
    }

    public PostRepository(FirebaseDatabase firebaseDatabase) {
        super(firebaseDatabase, REFERENCE_PATH);
    }

    public void insert
}
