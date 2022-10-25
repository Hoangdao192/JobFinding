package com.uet.fwork.database.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import lombok.Data;

@Data
public class CandidateModel extends UserModel {
    private String sex;
    private LocalDate dateOfBirth;

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
        this.dateOfBirth = LocalDate.parse(
                dateOfBirth,
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
        );
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = LocalDate.parse(
                dateOfBirth,
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
        );
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
