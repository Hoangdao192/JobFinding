package com.uet.fwork.firebasehelper;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class FirebaseAuthHelper {
    private FirebaseAuth firebaseAuth;

    public FirebaseAuthHelper(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
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
                        listener.onSuccess(signInMethodQueryResult.getSignInMethods());
                    }
                });
    }

    public interface OnSuccessListener<T> {
        void onSuccess(T result);
    }
}
