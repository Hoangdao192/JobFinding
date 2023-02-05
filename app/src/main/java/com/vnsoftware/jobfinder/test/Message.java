package com.vnsoftware.jobfinder.test;

import com.vnsoftware.jobfinder.database.model.chat.MessageContentModel;

import java.util.List;

public class Message {
    public String id;
    public List<MessageContentModel> contents;
    public String senderId;
    public Long sentTime;

    public Message() {
    }

    public Message(List<MessageContentModel> contents, String senderId, Long sentTime) {
        this.contents = contents;
        this.senderId = senderId;
        this.sentTime = sentTime;
    }

    public Message(String id, List<MessageContentModel> contents, String senderId, Long sentTime) {
        this.id = id;
        this.contents = contents;
        this.senderId = senderId;
        this.sentTime = sentTime;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", contents=" + contents +
                ", senderId='" + senderId + '\'' +
                ", sentTime=" + sentTime +
                '}';
    }
}
