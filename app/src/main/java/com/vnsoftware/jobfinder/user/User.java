package com.vnsoftware.jobfinder.user;

public class User {

    public String fullName, email, phoneNumber, major;

    public User() {

    }

    public User(String fullName, String email, String phoneNumber, String major) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.major = major;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getMajor() {
        return major;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
