package com.uet.fwork.database.model;

public class NotificationModel {
    private String notifyId = "";
    private String title = "";
    private String type = "";
    private String content = "";
    private Long sentTime = 0L;
    private String senderId = "";

    public NotificationModel(String notifyId, String title, String type, String content, Long sentTime, String senderId) {
        this.notifyId = notifyId;
        this.title = title;
        this.type = type;
        this.content = content;
        this.sentTime = sentTime;
        this.senderId = senderId;
    }

    public NotificationModel() {
    }

    public String getNotifyId() {
        return notifyId;
    }

    public void setNotifyId(String notifyId) {
        this.notifyId = notifyId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getSentTime() {
        return sentTime;
    }

    public void setSentTime(Long sentTime) {
        this.sentTime = sentTime;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    @Override
    public String toString() {
        return "NotificationModel{" +
                "notifyId='" + notifyId + '\'' +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", content='" + content + '\'' +
                ", sentTime=" + sentTime +
                ", senderId='" + senderId + '\'' +
                '}';
    }
}
