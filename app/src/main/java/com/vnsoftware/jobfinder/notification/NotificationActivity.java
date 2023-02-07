package com.vnsoftware.jobfinder.notification;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.vnsoftware.jobfinder.R;
import com.vnsoftware.jobfinder.database.model.NotificationModel;
import com.vnsoftware.jobfinder.database.repository.NotificationRepository;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private ImageView imgBack;
    private RecyclerView recNotification;
    private FirebaseUser firebaseUser;

    private NotificationRepository notificationRepository;
    private List<NotificationModel> notificationList = new ArrayList<>();

    public NotificationActivity() {
        super();
        this.notificationRepository = NotificationRepository.getInstance();
        this.firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        imgBack = (ImageView) findViewById(R.id.imgBack);

        recNotification = findViewById(R.id.recNotification);
        NotificationRecyclerViewAdapter adapter = new NotificationRecyclerViewAdapter(
                this, notificationList
        );
        recNotification.setAdapter(adapter);
        recNotification.setLayoutManager(new LinearLayoutManager(this));

        imgBack.setOnClickListener(imgView -> {
            finish();
        });

        notificationList.clear();
        notificationRepository.getRootDatabaseReference().child(firebaseUser.getUid())
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        notificationList.add(0, snapshot.getValue(NotificationModel.class));
                        adapter.notifyItemInserted(0);
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}