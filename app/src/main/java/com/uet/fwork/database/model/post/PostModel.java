package com.uet.fwork.database.model.post;

public class PostModel {

    private String postId;
    private String postName;
    private String postMajor;
    private String postAddress;
    private String postExperience;
    private String postSalary;
    private String postDescription;
    private String postTime;
    private String postImage;
    private String userId;
    private String userName;
    private String userEmail;
    private String userDp;

    public PostModel() {
    }

    public PostModel(
            String postId, String postName, String postMajor,
            String postAddress, String postTime, String postExperience,
            String postSalary, String postDescription, String postImage,
            String userId, String userName, String userEmail, String userDp) {
        this.postId = postId;
        this.postName = postName;
        this.postMajor = postMajor;
        this.postAddress = postAddress;
        this.postTime = postTime;
        this.postExperience = postExperience;
        this.postSalary = postSalary;
        this.postDescription = postDescription;
        this.postImage = postImage;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userDp = userDp;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPostName() {
        return postName;
    }

    public void setPostName(String postName) {
        this.postName = postName;
    }

    public String getPostMajor() {
        return postMajor;
    }

    public void setPostMajor(String postMajor) {
        this.postMajor = postMajor;
    }

    public String getPostAddress() {
        return postAddress;
    }

    public void setPostAddress(String postAddress) {
        this.postAddress = postAddress;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    public String getPostExperience() {
        return postExperience;
    }

    public void setPostExperience(String postExperience) {
        this.postExperience = postExperience;
    }

    public String getPostSalary() {
        return postSalary;
    }

    public void setPostSalary(String postSalary) {
        this.postSalary = postSalary;
    }

    public String getPostDescription() {
        return postDescription;
    }

    public void setPostDescription(String postDescription) {
        this.postDescription = postDescription;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserDp() {
        return userDp;
    }

    public void setUserDp(String userDp) {
        this.userDp = userDp;
    }
}
