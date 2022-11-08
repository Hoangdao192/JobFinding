package com.uet.fwork;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.uet.fwork.account.login.LoginActivity;
import com.uet.fwork.account.register.RegisterActivity;
import com.uet.fwork.chat.ChatActivity;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.model.chat.MessageContentModel;
import com.uet.fwork.database.model.chat.MessageModel;
import com.uet.fwork.database.repository.Repository;
import com.uet.fwork.database.repository.UserRepository;
import com.uet.fwork.firebasehelper.FirebaseSignInMethod;
import com.uet.fwork.test.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private UserRepository userRepository;

    public MainActivity() {
        super();
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.userRepository = new UserRepository(FirebaseDatabase.getInstance());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checking();
    }

    private void test() {
        List<MessageContentModel> messageContents = new ArrayList<>();
        messageContents.add(new MessageContentModel("0", "Text", "Message"));
        MessageModel messageModel = new MessageModel(
                messageContents,
                "123",
                System.currentTimeMillis() / 1000);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.getReference("chats/messages/-NG9fA0H_uMb39S1qbYr")
                .child("0").setValue(messageModel);
    }

    private void checking() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.reload().addOnSuccessListener(
                            new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    FirebaseAuth.getInstance().fetchSignInMethodsForEmail(firebaseUser.getEmail())
                                            .addOnSuccessListener(new OnSuccessListener<SignInMethodQueryResult>() {
                                                @Override
                                                public void onSuccess(SignInMethodQueryResult signInMethodQueryResult) {
                                                    List<String> methods = signInMethodQueryResult.getSignInMethods();
                                                    if (methods.contains(FirebaseSignInMethod.GOOGLE)
                                                            || methods.contains(FirebaseSignInMethod.FACEBOOK)) {
                                                        userRepository.getUserByUID(firebaseAuth.getUid(), new Repository.OnQuerySuccessListener<UserModel>() {
                                                            @Override
                                                            public void onSuccess(UserModel result) {
                                                                //  Người dùng chưa khai báo thông tin cá nhân
                                                                if (result.getLastUpdate() == 0) {
                                                                    Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                                                                    intent.putExtra("startDestinationId", R.id.selectUserRoleFragment);
                                                                    startActivity(intent);
                                                                } else {
                                                                    startDashboardActivity();
                                                                }
                                                            }
                                                        });
                                                    } else {
                                                        //  Kiểm tra email người dùng đã được xác mình chưa
                                                        if (!firebaseAuth.getCurrentUser().isEmailVerified()) {
                                                            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                                                            intent.putExtra("startDestinationId", R.id.registerVerifyRequestFragment);
                                                            startActivity(intent);
                                                        }
                                                        //  Kiểm tra người dùng đã khai báo thông tin cá nhân chưa
                                                        else {
                                                            userRepository.getUserByUID(firebaseAuth.getUid(), new Repository.OnQuerySuccessListener<UserModel>() {
                                                                @Override
                                                                public void onSuccess(UserModel result) {
                                                                    //  Người dùng chưa khai báo thông tin cá nhân
                                                                    if (result.getLastUpdate() == 0) {
                                                                        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                                                                        intent.putExtra("startDestinationId", R.id.selectUserRoleFragment);
                                                                        startActivity(intent);
                                                                    } else {
                                                                        startDashboardActivity();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                }
                                            });
                                    startDashboardActivity();
                                }
                            }
                    )
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                            if (e instanceof FirebaseAuthInvalidUserException) {
                                System.out.println("USER HAS BEEN DELETED");
                                startLoginActivity();
                            }
                        }
                    });
        } else {
            startLoginActivity();
        }
    }

    private void startDashboardActivity() {
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}