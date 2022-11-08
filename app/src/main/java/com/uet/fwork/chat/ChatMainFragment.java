package com.uet.fwork.chat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.uet.fwork.LoadingScreenDialog;
import com.uet.fwork.R;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.model.chat.MessageContentModel;
import com.uet.fwork.database.model.chat.MessageModel;
import com.uet.fwork.database.repository.ChatRepository;
import com.uet.fwork.database.repository.MessageRepository;
import com.uet.fwork.database.repository.Repository;
import com.uet.fwork.database.repository.UserRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatMainFragment extends Fragment {
    private NestedScrollView scrMessageList;
    private RecyclerView recMessageList;
    private TextView txtPartnerFullName;
    private EditText edtMessage;
    private Button btnSend;
    private CircleImageView imgUserAvatar;

    private String chatChanelId;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseStorage firebaseStorage;
    private ChatRepository chatRepository;
    private MessageRepository messageRepository;
    private UserRepository userRepository;

    private ImageView imgImage;

    private UserModel partnerUser;

    private List<MessageModel> messageList;
    private ActivityResultLauncher<Intent> getImageActivityLauncher;

    public ChatMainFragment() {
        super(R.layout.fragment_chat_message);
        messageList = new ArrayList<>();

        firebaseStorage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        chatRepository = new ChatRepository(FirebaseDatabase.getInstance());
        messageRepository = new MessageRepository(FirebaseDatabase.getInstance());
        userRepository = new UserRepository(FirebaseDatabase.getInstance());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        this.chatChanelId = bundle.getString("chatChanelId");
        System.out.println("CHATS: " + bundle.getSerializable("partner"));
        this.partnerUser = (UserModel) bundle.getSerializable("partner");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recMessageList = view.findViewById(R.id.recMessageList);
        txtPartnerFullName = view.findViewById(R.id.txtUserFullName);
        edtMessage = view.findViewById(R.id.edtMessage);
        btnSend = view.findViewById(R.id.btnSend);
        imgUserAvatar = view.findViewById(R.id.imgUserAvatar);
        imgImage = view.findViewById(R.id.imgImage);
        createImagePicker();

        imgImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                getImageActivityLauncher.launch(intent);
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!edtMessage.getText().toString().isEmpty()) {
                    String message = edtMessage.getText().toString();
                    edtMessage.getText().clear();
                    List<MessageContentModel> messageContents = new ArrayList<>();
                    messageContents.add(new MessageContentModel("0", "Text", message));
                    MessageModel messageModel = new MessageModel(
                            messageContents,
                            firebaseUser.getUid(),
                            System.currentTimeMillis() / 1000);
                    messageRepository.insertMessage(chatChanelId, messageModel, null);
                }
            }
        });

        //  Load data của user đối diện
        txtPartnerFullName.setText(partnerUser.getFullName());
        System.out.println(partnerUser.getFullName());
        System.out.println(partnerUser.getAvatar());
        if (partnerUser.getAvatar() != null && !partnerUser.getAvatar().equals("")) {
            Picasso.Builder builder = new Picasso.Builder(getContext());
            builder.listener(new Picasso.Listener() {
                @Override
                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                    System.out.println("PICASSO LOAD USER AVATAR ERROR");
                    builder.build().load(R.drawable.blank_user_avatar).into(imgUserAvatar);
                }
            });
            System.out.println("PICASSO LOAD PARTNER AVATAR");
            builder.build().load(partnerUser.getAvatar()).resize(100, 100).into(imgUserAvatar);
        } else {
            Picasso.get().load(R.drawable.blank_user_avatar).resize(100, 100).into(imgUserAvatar);
        }

        userRepository.getUserByUID(firebaseUser.getUid(), new Repository.OnQuerySuccessListener<UserModel>() {
            @Override
            public void onSuccess(UserModel user) {
                MessageListRecyclerViewAdapter adapter = new MessageListRecyclerViewAdapter(
                        FirebaseDatabase.getInstance(), getContext(), messageList,
                        chatChanelId,  user, partnerUser
                        );
                recMessageList.setAdapter(adapter);
                recMessageList.setLayoutManager(new LinearLayoutManager(getContext()));

                messageRepository.getRootDatabaseReference().child(chatChanelId)
                        .addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                loadMessages();
                            }

                            @Override
                            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                               loadMessages();
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

                            public void loadMessages() {
                                messageRepository.getAllMessageOrderBySentTimeLimit(chatChanelId,20L, messages -> {
                                    adapter.updateMessageList(messages);
                                    recMessageList.scrollToPosition(recMessageList.getAdapter().getItemCount() - 1);
                                });
                            }
                        });
            }
        });


    }

    private void createImagePicker() {
        this.getImageActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            createImageMessage(data.getData());
                        }
                    }
                }
        );
    }

    private void createImageMessage(Uri imageUri) {
        if (imageUri == null) {
            return;
        }

        List<MessageContentModel> messageContents = new ArrayList<>();
        messageContents.add(new MessageContentModel("0", "Text", ""));
        MessageModel messageModel = new MessageModel(
                messageContents,
                firebaseUser.getUid(),
                System.currentTimeMillis() / 1000);
        messageRepository.insertMessage(chatChanelId, messageModel, result -> {
            StorageReference storageReference = firebaseStorage.getReference("chats/" + chatChanelId + "/" + result);
            StorageReference imageReference = storageReference.child(firebaseAuth.getUid());
            imageReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                List<MessageContentModel> messageContents = new ArrayList<>();
                                messageContents.add(new MessageContentModel("0", "Image", uri.toString()));
                                Map<String, Object> updateData = new HashMap<>();
                                updateData.put("contents", messageContents);
                                messageRepository.updateMessage(chatChanelId, result, updateData);
                            }
                        });
                    })
                    .addOnFailureListener(Throwable::printStackTrace);
        });


    }

    public interface OnImagePickedListener {
        void onPicked(Uri imageUri);
    }
}
