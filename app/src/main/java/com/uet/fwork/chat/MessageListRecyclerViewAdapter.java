package com.uet.fwork.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.uet.fwork.R;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.model.chat.MessageContentModel;
import com.uet.fwork.database.model.chat.MessageModel;
import com.uet.fwork.database.model.chat.MessageStatus;
import com.uet.fwork.database.repository.MessageRepository;
import com.uet.fwork.database.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageListRecyclerViewAdapter extends RecyclerView.Adapter<MessageListRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<MessageModel> messageList;
    private FirebaseDatabase firebaseDatabase;
    private MessageRepository messageRepository;
    private UserRepository userRepository;
    private String chatChanelId;
    private MessageModel lastSeenMessage = null;
    private Drawable userAvatar = null, partnerAvatar = null;
    private Bitmap userBitmap = null, partnerBitmap = null;

    private List<Target> imageMessageTargetList = new ArrayList<>();

    private final static int MAX_IMAGE_WIDTH = 250;
    private final static int MAX_IMAGE_HEIGHT = 400;

    private Target userAvatarTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            userBitmap = bitmap;
            notifyDataSetChanged();
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
            System.out.println("BITMAP FAILED");
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };
    private Target partnerAvatarTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            partnerBitmap = bitmap;
            notifyDataSetChanged();
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    private UserModel user, partner;

    public MessageListRecyclerViewAdapter(
            FirebaseDatabase firebaseDatabase, Context context, List<MessageModel> messageList,
            String chatChanelId, UserModel user, UserModel partner) {
        this.context = context;
        this.messageList = messageList;
        this.user = user;
        this.partner = partner;
        this.firebaseDatabase = firebaseDatabase;
        this.chatChanelId = chatChanelId;
        this.messageRepository = new MessageRepository(firebaseDatabase);
        this.userRepository = new UserRepository(firebaseDatabase);

        if (user.getAvatar() != null && !user.getAvatar().equals("")) {
            System.out.println("LOAD BITMAP");
            Picasso.get().load(user.getAvatar()).into(userAvatarTarget);
        }

        if (partner.getAvatar() != null && !partner.getAvatar().equals("")) {
            Picasso.get().load(partner.getAvatar()).into(partnerAvatarTarget);
        }
    }

    public void setLastSeenMessage(MessageModel lastSeenMessage) {
        this.lastSeenMessage = lastSeenMessage;
    }

    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).getContents().get(0).getType().equals("Text")) {
            if (messageList.get(position).getSenderId().equals(user.getId())) return 1;
            return 2;
        } else if (messageList.get(position).getContents().get(0).getType().equals("Image")) {
            if (messageList.get(position).getSenderId().equals(user.getId())) return 3;
            else return 4;
        }
        return 0;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = null;
        switch (viewType) {
            case 1: view = layoutInflater.inflate(R.layout.item_recyclerview_chat_message_right, null); break;
            case 2: view = layoutInflater.inflate(R.layout.item_recyclerview_chat_message_left, null); break;
            case 3: view = layoutInflater.inflate(R.layout.item_recyclerview_chat_message_image_right, null); break;
            case 4: view = layoutInflater.inflate(R.layout.item_recyclerview_chat_message_image_left, null); break;
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessageModel messageModel = messageList.get(position);
        if (messageModel.getContents().get(0).getType().equals("Text")) {
            String message = "";
            StringBuilder stringBuilder = new StringBuilder();
            messageModel.getContents().forEach(contentModel -> {
                stringBuilder.append(contentModel.getContent());
            });
            holder.txtMessage.setText(stringBuilder.toString());
        } else if (messageModel.getContents().get(0).getType().equals("Image") && holder.imgImage != null) {
            String imagePath = messageModel.getContents().get(0).getContent();
            if (imagePath != null || !imagePath.isEmpty()) {
                System.out.println(imagePath);
                Picasso.Builder builder = new Picasso.Builder(context);
                Target target = new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        Bitmap resizeBitmap = bitmap;
                        if (bitmap.getWidth() > MAX_IMAGE_WIDTH) {
                            resizeBitmap = Bitmap.createScaledBitmap(bitmap, MAX_IMAGE_WIDTH,
                                    MAX_IMAGE_WIDTH * bitmap.getHeight() / bitmap.getWidth(), false);
                        } else if (bitmap.getHeight() > MAX_IMAGE_HEIGHT) {
                            resizeBitmap = Bitmap.createScaledBitmap(bitmap,
                                    MAX_IMAGE_HEIGHT * bitmap.getWidth() / bitmap.getHeight(),
                                    MAX_IMAGE_HEIGHT, false);
                        }
                        System.out.println("RUNNED");
                        holder.imgImage.setImageBitmap(resizeBitmap);
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                };
                imageMessageTargetList.add(target);
                builder.build().load(imagePath)
                        .placeholder(R.drawable.loading_animation)
                        .into(target);
            }
        }


        if (messageModel.getSenderId().equals(user.getId()) && userBitmap != null) {
            holder.imgAvatar.setImageBitmap(userBitmap);
        } else if (partnerBitmap != null) {
            holder.imgAvatar.setImageBitmap(partnerBitmap);
        }
    }

    public void updateMessageList(List<MessageModel> messageList) {
        this.messageList.clear();
        this.imageMessageTargetList.clear();
        this.messageList.addAll(messageList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtMessage;
        private CircleImageView imgAvatar;
        private ImageView imgImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgUserAvatar);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            imgImage = itemView.findViewById(R.id.imgImage);
        }
    }
}
