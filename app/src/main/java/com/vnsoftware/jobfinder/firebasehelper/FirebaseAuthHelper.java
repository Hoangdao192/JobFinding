package com.vnsoftware.jobfinder.firebasehelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.GraphRequest;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.FirebaseDatabase;
import com.vnsoftware.jobfinder.database.model.UserModel;
import com.vnsoftware.jobfinder.database.repository.UserRepository;

import org.json.JSONException;

import java.util.List;

public class FirebaseAuthHelper {
    private static final String LOG_TAG = "FirebaseAuthHelper";

    private UserModel user = null;
    private String signInMethod = null;

    private FirebaseAuth firebaseAuth = null;
    private FirebaseDatabase firebaseDatabase = null;
    private Context applicationContext = null;

    private static FirebaseAuthHelper INSTANCE = null;

    private FirebaseAuthHelper(Context applicationContext, FirebaseAuth firebaseAuth, FirebaseDatabase firebaseDatabase) {
        this.firebaseAuth = firebaseAuth;
        this.firebaseDatabase = firebaseDatabase;
        this.applicationContext = applicationContext;
    }

    /**
     * You must call initialize method before call this method
     * If method initialize has not been called yet
     * @return null
     * Else
     * @return INSTANCE
     */
    public static FirebaseAuthHelper getInstance() {
        if (INSTANCE == null) {
            Log.e(LOG_TAG, "FirebaseAuthHelper has not been initialized yet");
            return null;
        }

        return INSTANCE;
    }

    /**
     * You must call this method before call getInstance() method
     * @param firebaseDatabase
     * @param firebaseAuth
     * @param context
     */
    public static void initialize(
            FirebaseDatabase firebaseDatabase, FirebaseAuth firebaseAuth,
            Context context) {
        if (INSTANCE != null) {
            Log.d(LOG_TAG, "FirebaseAuthHelper has been initialized already");
            return;
        }

        INSTANCE = new FirebaseAuthHelper(
                context.getApplicationContext(), firebaseAuth, firebaseDatabase
        );
    }

    public void fetchCurrentUserData(OnSuccessListener<Boolean> listener) {
        UserRepository userRepository = UserRepository.getInstance();
        userRepository.getUserByUID(firebaseAuth.getUid(), user -> {
            if (user == null) {
                Log.d(LOG_TAG, "FirebaseAuthHelper: Fetch user null");
                return;
            }

            firebaseAuth.fetchSignInMethodsForEmail(firebaseAuth.getCurrentUser().getEmail())
                    .addOnSuccessListener(signInMethodQueryResult -> {
                        INSTANCE.user = user;
                        INSTANCE.signInMethod = signInMethodQueryResult.getSignInMethods().get(0);
                        listener.onSuccess(true);
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        Log.d("FirebaseAuthHelper", "Firebase auth: Fetch sign in method failed");
                        listener.onSuccess(false);
                    })
                    .addOnCanceledListener(() ->
                            Log.e(LOG_TAG, "Firebase auth: Fetch sign in method cancelled"));
        });
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    public String getSignInMethod() {
        return signInMethod;
    }

    public void setSignInMethod(String signInMethod) {
        this.signInMethod = signInMethod;
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
