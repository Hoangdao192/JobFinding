package com.uet.fwork.database.model.chat;

import androidx.annotation.NonNull;

public class MessageModel {
    private @NonNull String id;
    private @NonNull String type;
    private @NonNull String content;
    private @NonNull String senderId;
    private @NonNull Long sentTime;

    public MessageModel() {
    }

    public MessageModel(
            @NonNull String type, @NonNull String content,
            @NonNull String senderId, @NonNull Long sentTime) {
        this.type = type;
        this.content = content;
        this.senderId = senderId;
        this.sentTime = sentTime;
    }

    public MessageModel(
            @NonNull String id, @NonNull String type,
            @NonNull String content, @NonNull String senderId, @NonNull Long sentTime) {
        this.id = id;
        this.type = type;
        this.content = content;
        this.senderId = senderId;
        this.sentTime = sentTime;
    }

    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(@NonNull String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(@NonNull String content) {
        this.content = content;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(@NonNull String senderId) {
        this.senderId = senderId;
    }

    public Long getSentTime() {
        return sentTime;
    }

    public void setSentTime(@NonNull Long sentTime) {
        this.sentTime = sentTime;
    }
}
