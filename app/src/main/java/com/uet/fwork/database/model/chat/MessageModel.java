package com.uet.fwork.database.model.chat;

import java.util.List;
import java.util.Map;

public class MessageModel {
    private String id;
    private List<MessageContentModel> contents;
    private String senderId;
    private Long sentTime;
    private String status = MessageStatus.NOT_SET;

    public MessageModel() {
    }

    public MessageModel(List<MessageContentModel> contents, String senderId, Long sentTime) {
        this.contents = contents;
        this.senderId = senderId;
        this.sentTime = sentTime;
    }

    public MessageModel(List<MessageContentModel> contents, String senderId, Long sentTime, String status) {
        this.contents = contents;
        this.senderId = senderId;
        this.sentTime = sentTime;
        this.status = status;
    }

    public MessageModel(String id, List<MessageContentModel> contents, String senderId, Long sentTime) {
        this.id = id;
        this.contents = contents;
        this.senderId = senderId;
        this.sentTime = sentTime;
    }

    public MessageModel(String id, List<MessageContentModel> contents, String senderId, Long sentTime, String status) {
        this.id = id;
        this.contents = contents;
        this.senderId = senderId;
        this.sentTime = sentTime;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<MessageContentModel> getContents() {
        return contents;
    }

    public void setContents(List<MessageContentModel> contents) {
        this.contents = contents;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public Long getSentTime() {
        return sentTime;
    }

    public void setSentTime(Long sentTime) {
        this.sentTime = sentTime;
    }
}
