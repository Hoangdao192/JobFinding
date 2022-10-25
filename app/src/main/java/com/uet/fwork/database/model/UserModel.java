package com.uet.fwork.database.model;

import lombok.Data;

@Data
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
}
