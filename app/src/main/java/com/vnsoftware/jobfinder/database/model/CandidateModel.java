package com.vnsoftware.jobfinder.database.model;

public class CandidateModel extends UserModel {
    private String sex = "";
    private String dateOfBirth = "";
    private String major = "";
    private double yearOfExperience = 0;

    public CandidateModel() {
        super();
    }

    public CandidateModel(
            String id, String email,
            String avatar, String fullName, String phoneNumber,
            String contactEmail, String sex, /*dd/MM/yyyy*/ String dateOfBirth,
            String major, double yearOfExperience
    ) {
        super(id, email, avatar, fullName, phoneNumber, contactEmail, UserRole.CANDIDATE);
        this.sex = sex;
        this.dateOfBirth = dateOfBirth;
        this.major = major;
        this.yearOfExperience = yearOfExperience;
    }

    public CandidateModel(
            String id, String email,
            String avatar, String fullName, String phoneNumber,
            String contactEmail, String sex, /*dd/MM/yyyy*/ String dateOfBirth,
            String major, double yearOfExperience, long lastUpdate
    ) {
        this(id, email, avatar, fullName, phoneNumber, contactEmail, sex, dateOfBirth,major, yearOfExperience);
        this.setLastUpdate(lastUpdate);
    }


    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public double getYearOfExperience() {
        return yearOfExperience;
    }

    public void setYearOfExperience(double yearOfExperience) {
        this.yearOfExperience = yearOfExperience;
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
