package com.uet.fwork.database.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CandidateModel extends UserModel {
    private String sex;
    private String dateOfBirth;

    public CandidateModel() {
        super();
    }

    public CandidateModel(
            String id, String email,
            String avatar, String fullName, String phoneNumber,
            String contactEmail, String sex, /*dd/MM/yyyy*/ String dateOfBirth
    ) {
        super(id, email, avatar, fullName, phoneNumber, contactEmail, UserRole.CANDIDATE);
        this.sex = sex;
        this.dateOfBirth = dateOfBirth;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getDateOfBirth() {
        return this.dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    @Override
    public String toString() {
        return "CandidateModel{" +
                "sex='" + sex + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", avatar='" + avatar + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", contactEmail='" + contactEmail + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
