package com.uet.fwork.database.repository;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public abstract class Repository {
    private static final String LOG_TAG = "Repository";

    protected DatabaseReference rootDatabaseReference;
    protected String referencePath = "";

    protected static Context applicationContext = null;
    protected static FirebaseDatabase firebaseDatabase = null;

    /**
     * You must call this before use any Repository
     * @param context
     * @param firebaseDatabase
     */
    public static void initialize(Context context, FirebaseDatabase firebaseDatabase) {
        if (Repository.applicationContext != null && Repository.firebaseDatabase != null) {
            Log.d(LOG_TAG, "Repository has been initialized already");
            return;
        }

        Repository.applicationContext = context.getApplicationContext();
        Repository.firebaseDatabase = firebaseDatabase;
    }

    public static boolean isInitialize() {
        return applicationContext != null && firebaseDatabase != null;
    }

    protected Repository() {
        this.rootDatabaseReference = firebaseDatabase.getReference(referencePath);
    }


    protected Repository(String referencePath) {
        this.referencePath = referencePath;
        this.rootDatabaseReference = firebaseDatabase.getReference(referencePath);
    }

    public FirebaseDatabase getFirebaseDatabase() {
        return firebaseDatabase;
    }

    public DatabaseReference getRootDatabaseReference() {
        return rootDatabaseReference;
    }

    public interface OnQuerySuccessListener<T> {
        void onSuccess(T result);
    }
}
