package com.uet.fwork.database.model;

public class UserModel {
    protected String id;
    protected String email;
    protected String avatar;
    protected String fullName;
    protected String phoneNumber;
    protected String contactEmail;
    protected String role;

    public UserModel() {
    }

    public UserModel(
            String id, String email, String avatar,
            String fullName, String phoneNumber, String contactEmail,
            String role
    ) {
        this.id = id;
        this.email = email;
        this.avatar = avatar;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.contactEmail = contactEmail;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", avatar='" + avatar + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", contactEmail='" + contactEmail + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
