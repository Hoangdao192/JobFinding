package com.vnsoftware.jobfinder.notification;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class NotificationFragment extends Fragment {

    private ImageButton imgBtnBack;
    private RecyclerView recNotification;
    private FirebaseUser firebaseUser;

    private NotificationRepository notificationRepository;
    private List<NotificationModel> notificationList = new ArrayList<>();

    public NotificationFragment() {
        super(R.layout.fragment_notification);
        this.notificationRepository = NotificationRepository.getInstance();
        this.firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        imgBtnBack = view.findViewById(R.id.imgBtnBack);
        recNotification = view.findViewById(R.id.recNotification);
        NotificationRecyclerViewAdapter adapter = new NotificationRecyclerViewAdapter(
                    getContext(), notificationList
            );
        recNotification.setAdapter(adapter);
        recNotification.setLayoutManager(new LinearLayoutManager(getContext()));

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
