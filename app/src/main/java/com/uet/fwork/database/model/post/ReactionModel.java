package com.uet.fwork.database.model.post;

public class ReactionModel {
    private String reactionId = "";
    private String userId = "";
    private String postId = "";
    private Long sentTime = 0L;

    public ReactionModel() {
    }

    public ReactionModel(String userId, String postId, Long sentTime) {
        this.userId = userId;
        this.postId = postId;
        this.sentTime = sentTime;
    }

    public ReactionModel(String reactionId, String userId, String postId, Long sentTime) {
        this.reactionId = reactionId;
        this.userId = userId;
        this.postId = postId;
        this.sentTime = sentTime;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getReactionId() {
        return reactionId;
    }

    public void setReactionId(String reactionId) {
        this.reactionId = reactionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getSentTime() {
        return sentTime;
    }

    public void setSentTime(Long sentTime) {
        this.sentTime = sentTime;
    }
}
