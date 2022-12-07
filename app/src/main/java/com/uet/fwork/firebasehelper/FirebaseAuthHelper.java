package com.uet.fwork.firebasehelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.FirebaseDatabase;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.repository.Repository;
import com.uet.fwork.database.repository.UserRepository;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class FirebaseAuthHelper {
    private FirebaseAuth firebaseAuth;
    private static  UserModel user = null;
    private static String signInMethod = null;
    private static UserRepository userRepository;

    private static Context applicationContext = null;

    public FirebaseAuthHelper(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    public FirebaseAuthHelper(FirebaseAuth firebaseAuth, Context context) {
        this.firebaseAuth = firebaseAuth;
        this.applicationContext = context.getApplicationContext();
    }

    public static final void initialize(
            FirebaseDatabase firebaseDatabase, FirebaseAuth firebaseAuth,
            Context context, OnSuccessListener<Boolean> listener) {
        userRepository = new UserRepository(firebaseDatabase);
        applicationContext = context.getApplicationContext();
        userRepository.getUserByUID(firebaseAuth.getUid(), new Repository.OnQuerySuccessListener<UserModel>() {
            @Override
            public void onSuccess(UserModel userModel) {
                user = userModel;
                firebaseAuth.fetchSignInMethodsForEmail(firebaseAuth.getCurrentUser().getEmail())
                        .addOnSuccessListener(signInMethodQueryResult -> {
                            signInMethod = signInMethodQueryResult.getSignInMethods().get(0);
                            listener.onSuccess(true);
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.printStackTrace();
                                Log.d("FirebaseAuthHelper", "Firebase auth: Fetch sign in method failed");
                            }
                        });
            }
        });


    }

    public static UserModel getUser() {
        return user;
    }

    public static String getSignInMethod() {
        return signInMethod;
    }

    public void isUserWithEmailExists(String email, OnSuccessListener<Boolean> onSuccessListener) {
        firebaseAuth.fetchSignInMethodsForEmail(email)
                .addOnSuccessListener(signInMethodQueryResult -> {
                    List<String> signInMethods = signInMethodQueryResult.getSignInMethods();
                    signInMethods.forEach(System.out::println);
                    onSuccessListener.onSuccess(!signInMethods.isEmpty());
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    public void isFacebookUserExistsInFirebase(LoginResult loginResult, OnSuccessListener<Boolean> onSuccessListener) {
        GraphRequest request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(),
                (object, response) -> {
                    // Application code
                    try {
                        String email = object.getString("email");
                        isUserWithEmailExists(email, new OnSuccessListener<Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) {
                                if (result) {
                                    isUserWithEmailAndSignInMethodExists(email, FirebaseSignInMethod.FACEBOOK, onSuccessListener);
                                } else {
                                    onSuccessListener.onSuccess(false);
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    /**
     * Kiểm tra user với email và phương thức xác thực tương ứng có tồn tại không
     * @param email
     * @param signInMethod
     * @param onSuccessListener
     */
    public void isUserWithEmailAndSignInMethodExists(
            String email, String signInMethod, OnSuccessListener<Boolean> onSuccessListener) {
        firebaseAuth.fetchSignInMethodsForEmail(email)
                .addOnSuccessListener(signInMethodQueryResult -> {
                    List<String> signInMethods = signInMethodQueryResult.getSignInMethods();
                    if (!signInMethods.isEmpty() && signInMethods.contains(signInMethod)) {
                        onSuccessListener.onSuccess(true);
                    }
                    else {
                        onSuccessListener.onSuccess(false);
                    }
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    public void getUserSignInMethod(String email, OnSuccessListener<List<String>> listener) {
        firebaseAuth.fetchSignInMethodsForEmail(email)
                .addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<SignInMethodQueryResult>() {
                    @Override
                    public void onSuccess(SignInMethodQueryResult signInMethodQueryResult) {
                        if (signInMethodQueryResult.getSignInMethods().size() == 0) {
                            listener.onSuccess(null);
                        } else listener.onSuccess(signInMethodQueryResult.getSignInMethods());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        listener.onSuccess(null);
                    }
                });
    }

    public void signOut() {
        if (applicationContext != null) {
            SharedPreferences sharedPreferences = applicationContext.getSharedPreferences("MAIN", Context.MODE_PRIVATE);
            sharedPreferences.edit().remove("USER").apply();
        }
        firebaseAuth.signOut();
    }

    public interface OnSuccessListener<T> {
        void onSuccess(T result);
    }
}
