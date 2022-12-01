package com.uet.fwork.database.model.post;

public class PostApplyModel {
    private String applyId = "";
    private String postId = "";
    private String userId = "";
    private Long applyTime = 0L;

    public PostApplyModel() {
    }

    public PostApplyModel(String postId, String userId, Long applyTime) {
        this.postId = postId;
        this.userId = userId;
        this.applyTime = applyTime;
    }

    public PostApplyModel(String applyId, String postId, String userId, Long applyTime) {
        this.applyId = applyId;
        this.postId = postId;
        this.userId = userId;
        this.applyTime = applyTime;
    }

    public String getApplyId() {
        return applyId;
    }

    public void setApplyId(String applyId) {
        this.applyId = applyId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(Long applyTime) {
        this.applyTime = applyTime;
    }

    @Override
    public String toString() {
        return "PostApplyModel{" +
                "applyId='" + applyId + '\'' +
                ", postId='" + postId + '\'' +
                ", userId='" + userId + '\'' +
                ", applyTime=" + applyTime +
                '}';
    }
}
