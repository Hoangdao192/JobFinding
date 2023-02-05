package com.vnsoftware.jobfinder.database.model.post;

public class CommentModel {
    private String commentId = "";
    private Long commentTime = 0L;
    private String content = "";
    private String postId = "";
    private String userId = "";
    private String replyTo = "";

    public CommentModel() {
    }

    public CommentModel(
            String commentId, Long commentTime, String content,
            String postId, String userId, String replyTo) {
        this.commentId = commentId;
        this.commentTime = commentTime;
        this.content = content;
        this.postId = postId;
        this.userId = userId;
        this.replyTo = replyTo;
    }

    public CommentModel(
            Long commentTime, String content, String postId, String userId, String replyTo) {
        this.commentTime = commentTime;
        this.content = content;
        this.postId = postId;
        this.userId = userId;
        this.replyTo = replyTo;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public Long getCommentTime() {
        return commentTime;
    }

    public void setCommentTime(Long commentTime) {
        this.commentTime = commentTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }
}
