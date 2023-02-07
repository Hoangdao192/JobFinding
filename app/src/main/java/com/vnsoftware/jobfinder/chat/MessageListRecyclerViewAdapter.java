package com.vnsoftware.jobfinder.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.vnsoftware.jobfinder.R;
import com.vnsoftware.jobfinder.database.model.UserModel;
import com.vnsoftware.jobfinder.database.model.chat.MessageModel;
import com.vnsoftware.jobfinder.database.repository.UserRepository;
import com.vnsoftware.jobfinder.util.DpToPixelConverter;
import com.vnsoftware.jobfinder.util.ImageHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageListRecyclerViewAdapter extends RecyclerView.Adapter<MessageListRecyclerViewAdapter.ViewHolder> {

    private final Context context;
    private final List<MessageModel> messageList;
    private UserRepository userRepository;
    private String chatChanelId;
    private Bitmap userBitmap = null, partnerBitmap = null;

    private List<Target> imageMessageTargetList = new ArrayList<>();
    private Map<String, Bitmap> messageImageMap = new HashMap<>();

    private final int MAX_IMAGE_WIDTH;
    private final int MAX_IMAGE_HEIGHT;
    private final int MAX_MESSAGE_WIDTH;

    private DisplayMetrics displayMetrics;

    private Target userAvatarTarget;
    private Target partnerAvatarTarget;

    private UserModel user, partner;

    public MessageListRecyclerViewAdapter(
            FirebaseDatabase firebaseDatabase, Context context, List<MessageModel> messageList,
            String chatChanelId, UserModel user, UserModel partner) {
        this.displayMetrics = context.getResources().getDisplayMetrics();
        this.context = context;
        this.messageList = messageList;
        this.user = user;
        this.partner = partner;
        this.chatChanelId = chatChanelId;
        this.userRepository = UserRepository.getInstance();

        userAvatarTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                userBitmap = bitmap;
                notifyDataSetChanged();
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        partnerAvatarTarget = new Target() {
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

        if (user.getAvatar() != null && !user.getAvatar().equals("")) {
            Picasso.get().load(user.getAvatar()).into(userAvatarTarget);
        }

        if (partner.getAvatar() != null && !partner.getAvatar().equals("")) {
            Picasso.get().load(partner.getAvatar()).into(partnerAvatarTarget);
        }

        MAX_MESSAGE_WIDTH = this.displayMetrics.widthPixels * 3 / 5;
        MAX_IMAGE_WIDTH = MAX_MESSAGE_WIDTH;
        MAX_IMAGE_HEIGHT = DpToPixelConverter.convertDpToPixels(context, 400);
    }

    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).getType().equals("Text")) {
            if (messageList.get(position).getSenderId().equals(user.getId())) return 1;
            return 2;
        } else if (messageList.get(position).getType().equals("Image")) {
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
        Log.d("CHAT", "Load message list.");

        MessageModel messageModel = messageList.get(position);
        //  Nếu tin nhắn là dạng Text
        if (messageModel.getType().equals("Text") && holder.txtMessage != null) {
            holder.txtMessage.setText(messageModel.getContent());
            holder.txtMessage.setMaxWidth(MAX_MESSAGE_WIDTH);
        }
        //  Nếu tin nhắn ở dạng Image
        else if (messageModel.getType().equals("Image") && holder.imgImage != null) {
            String imagePath = messageModel.getContent();
            if (imagePath != null && !imagePath.isEmpty()) {
                //  Nếu ảnh đã được tải về
                if (messageImageMap.containsKey(messageModel.getId())) {
                    holder.imgImage.setImageBitmap(messageImageMap.get(messageModel.getId()));
                }
                //  Ảnh chưa được tải về
                else {
                    Picasso.Builder builder = new Picasso.Builder(context);
                    Target target = new Target() {
                        /**
                         * Resize image
                         */
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            Bitmap resizeBitmap = ImageHelper.reduceImageSize(bitmap, MAX_MESSAGE_WIDTH, MAX_IMAGE_HEIGHT);
                            holder.imgImage.setImageBitmap(resizeBitmap);
                            messageImageMap.put(messageModel.getId(), resizeBitmap);
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    };
                    //  Target sẽ bị GC dọn dẹp nếu không lưu lại
                    imageMessageTargetList.add(target);
                    holder.imgImage.setImageDrawable(context.getDrawable(R.drawable.image_holder));
                    builder.build().load(imagePath).into(target);
                }
            }
        }

        if (holder.getItemViewType() != 1) {
            //  Đặt ảnh avatar của người gửi cho message
            boolean lastInGroup = false;
            if (position == messageList.size() - 1) {
                lastInGroup = true;
            } else if (position < messageList.size() - 1
                    && !messageList.get(position + 1).getSenderId().equals(messageModel.getSenderId())) {
                lastInGroup = true;
            }
            if (lastInGroup) {
                holder.imgAvatar.setVisibility(View.VISIBLE);
                if (messageModel.getSenderId().equals(user.getId()) && userBitmap != null) {
                    holder.imgAvatar.setImageBitmap(userBitmap);
                } else if (!messageModel.getSenderId().equals(user.getId()) && partnerBitmap != null) {
                    holder.imgAvatar.setImageBitmap(partnerBitmap);
                }

                //  Convert dp to pixel
                holder.view.setPadding(
                        holder.view.getPaddingLeft(),
                        0,
                        holder.view.getPaddingRight(),
                        DpToPixelConverter.convertDpToPixels(context, 10)
                );
            } else {
                holder.imgAvatar.setVisibility(View.INVISIBLE);
                holder.view.setPadding(
                        holder.view.getPaddingLeft(),
                        0,
                        holder.view.getPaddingRight(),
                        0
                );
            }
        }

        if (position == 0) {
            holder.view.setPadding(
                    holder.view.getPaddingLeft(),
                    DpToPixelConverter.convertDpToPixels(context, 10),
                    holder.view.getPaddingRight(),
                    holder.view.getPaddingBottom()
            );
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private TextView txtMessage;
        private CircleImageView imgAvatar;
        private ImageView imgImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            imgAvatar = itemView.findViewById(R.id.imgUserAvatar);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            imgImage = itemView.findViewById(R.id.imgImage);
        }
    }
}
