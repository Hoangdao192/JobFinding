package com.uet.fwork;

public class PostModel {

    String postId, postName, postMajor, postAddress, postExperience, postSalary, postDescription, postTime, postImage, uid, userName, userEmail, userDp;

    public PostModel() {
    }

    public PostModel(String postId, String postName, String postMajor, String postAddress, String postTime, String postExperience,
                     String postSalary, String postDescription, String postImage, String uid, String userName, String userEmail, String userDp) {
        this.postId = postId;
        this.postName = postName;
        this.postMajor = postMajor;
        this.postAddress = postAddress;
        this.postTime = postTime;
        this.postExperience = postExperience;
        this.postSalary = postSalary;
        this.postDescription = postDescription;
        this.postImage = postImage;
        this.uid = uid;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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
