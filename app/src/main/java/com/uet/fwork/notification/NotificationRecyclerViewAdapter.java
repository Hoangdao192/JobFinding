package com.uet.fwork.notification;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.L;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.uet.fwork.R;
import com.uet.fwork.database.model.NotificationModel;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.repository.UserRepository;
import com.uet.fwork.util.TimestampToString;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationRecyclerViewAdapter extends RecyclerView.Adapter<NotificationRecyclerViewAdapter.ViewHolder> {

    private UserRepository userRepository;
    private Map<String, UserModel> userMap = new HashMap<>();

    private Context context;
    private List<NotificationModel> notificationList;

    public NotificationRecyclerViewAdapter(Context context, List<NotificationModel> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
        Collections.reverse(this.notificationList);
        this.userRepository = new UserRepository(FirebaseDatabase.getInstance());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.item_recyclerview_notification_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationModel notification = notificationList.get(position);
        holder.txvMessage.setText(notification.getContent());
        holder.txvSentTime.setText(TimestampToString.convert(notification.getSentTime()) + " trước");
        if (!userMap.containsKey(notification.getSenderId())) {
            userRepository.getUserByUID(notification.getSenderId(), user -> {
                userMap.put(notification.getSenderId(), user);
                loadDataIntoHolder(holder, user, notification);
            });
        } else  {
            loadDataIntoHolder(holder, userMap.get(notification.getSenderId()), notification);
        }
    }

    private void loadDataIntoHolder(ViewHolder holder, UserModel sender, NotificationModel notification) {
        if (!sender.getAvatar().equals("")) {
            Picasso.get().load(sender.getAvatar()).into(holder.cirImgAvatar);
        }
        holder.txvSenderName.setText(sender.getFullName());
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView cirImgAvatar;
        private TextView txvSenderName;
        private TextView txvMessage;
        private TextView txvSentTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cirImgAvatar = itemView.findViewById(R.id.cirImgAvatar);
            txvSenderName = itemView.findViewById(R.id.txvSenderName);
            txvMessage = itemView.findViewById(R.id.txvMessage);
            txvSentTime = itemView.findViewById(R.id.txvSentTime);
        }
    }
}
