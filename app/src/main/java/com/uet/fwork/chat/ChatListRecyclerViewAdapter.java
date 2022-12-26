package com.uet.fwork.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.uet.fwork.R;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.model.chat.ChanelModel;
import com.uet.fwork.database.model.chat.MessageModel;
import com.uet.fwork.database.repository.ChatRepository;
import com.uet.fwork.database.repository.MessageRepository;
import com.uet.fwork.database.repository.Repository;
import com.uet.fwork.database.repository.UserRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListRecyclerViewAdapter extends RecyclerView.Adapter<ChatListRecyclerViewAdapter.ViewHolder> {

    private List<String> chanelIdList;
    private Map<String, UserModel> userMap = new HashMap<>();
    private Map<String, Bitmap> userAvatarMap = new HashMap<>();
    private Context context;

    private String currentUserId;
    private UserRepository userRepository;
    private ChatRepository chatRepository;
    private MessageRepository messageRepository;

    private List<Target> targetList = new ArrayList<>();

    private OnItemClickListener listener = null;

    public ChatListRecyclerViewAdapter(Context context, List<String> chanelIdList,
                                       String currentUserId, FirebaseDatabase firebaseDatabase,
                                       @Nullable OnItemClickListener listener) {
        this.context = context;
        this.chanelIdList = chanelIdList;
        this.currentUserId = currentUserId;
        this.userRepository = UserRepository.getInstance();
        this.chatRepository = ChatRepository.getInstance();
        this.messageRepository = MessageRepository.getInstance();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.item_recyclerview_chat_list, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String chanelId = chanelIdList.get(position);
        chatRepository.getChatChanelById(chanelId, new Repository.OnQuerySuccessListener<ChanelModel>() {
            @Override
            public void onSuccess(ChanelModel chanel) {
                chanel.getMembers().forEach(memberId -> {
                    if (!memberId.equals(currentUserId)) {
                        if (!userMap.containsKey(memberId)) {
                            //  Lấy thông tin user
                            userRepository.getUserByUID(memberId, user -> {
                                userMap.put(memberId, user);
                                if (user.getAvatar() != null && !user.getAvatar().equals("")) {
                                    Target target = new Target() {
                                        @Override
                                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                            holder.imgAvatar.setImageBitmap(bitmap);
                                            userAvatarMap.put(memberId, bitmap);
                                        }

                                        @Override
                                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                                        }

                                        @Override
                                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                                        }
                                    };
                                    targetList.add(target);
                                    Picasso.get().load(user.getAvatar())
                                            .resize(100, 100)
                                            .into(target);
                                } else {
                                    holder.imgAvatar.setImageDrawable(context.getDrawable(R.drawable.default_avatar));
                                }

//                                if (!user.getAvatar().isEmpty()) {
//                                    Picasso.get().load(user.getAvatar())
//                                            .placeholder(R.drawable.wlop_33se)
//                                            .into(holder.imgAvatar);
//                                } else {
//                                    holder.imgAvatar.setImageDrawable(context.getDrawable(R.drawable.default_avatar));
//                                }
                                holder.userName.setText(user.getFullName());
                                messageRepository.getLastMessage(chanelId, messageModel -> {
                                    if (messageModel != null) {
                                        holder.txvLastMessage.setText(messageModel.getContent());
                                    }
                                });
                            });
                        }
                        else {
                            holder.userName.setText(userMap.get(memberId).getFullName());
                            if (userAvatarMap.containsKey(memberId)) {
                                holder.imgAvatar.setImageBitmap(userAvatarMap.get(memberId));
                            } else {
                                holder.imgAvatar.setImageDrawable(context.getDrawable(R.drawable.default_avatar));
                            }
                            messageRepository.getLastMessage(chanelIdList.get(holder.getBindingAdapterPosition()), messageModel -> {
                                if (messageModel != null) {
                                    holder.txvLastMessage.setText(messageModel.getContent());
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return chanelIdList.size();
    }

    public void updateChanelList(List<String> newChanelIdList) {
        this.chanelIdList.clear();
        this.chanelIdList.addAll(newChanelIdList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView imgAvatar;
        private TextView userName, txvLastMessage;

        public ViewHolder (@NonNull View view, @Nullable OnItemClickListener listener) {
            super(view);
            imgAvatar = view.findViewById(R.id.imgAvatar);
            userName = view.findViewById(R.id.txtFullName);
            txvLastMessage = view.findViewById(R.id.txtLastMessage);
            if (listener != null) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        chatRepository.getChatChanelById(chanelIdList.get(getAdapterPosition()),
                                new Repository.OnQuerySuccessListener<ChanelModel>() {
                            @Override
                            public void onSuccess(ChanelModel result) {
                                listener.onClick(result);
                            }
                        });

                    }
                });
            }
        }
    }

    public interface OnItemClickListener {
        void onClick(ChanelModel chanel);
    }
}
