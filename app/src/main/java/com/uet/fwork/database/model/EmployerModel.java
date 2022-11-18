package com.uet.fwork.database.model;

public class EmployerModel extends UserModel {
    private AddressModel address;
    private String description = "";

    public EmployerModel() {
        super();
    }

    public EmployerModel(
            String id, String email, String avatar,
            String fullName, String phoneNumber, String contactEmail,
            String role, long lastUpdate,
            String description,
            AddressModel address) {
        super(id, email, avatar, fullName, phoneNumber, contactEmail, role, lastUpdate);
        this.address = address;
        this.description = description;
    }

    public EmployerModel(
            String id, String email,
            String avatar, String fullName, String phoneNumber,
            String contactEmail, AddressModel addressModel
    ) {
        super(id, email, avatar, fullName, phoneNumber, contactEmail, UserRole.EMPLOYER);
        this.address = addressModel;
    }

    public EmployerModel(
            String id, String email,
            String avatar, String fullName, String phoneNumber,
            String contactEmail, AddressModel addressModel, long lastUpdate
    ) {
        this(id, email, avatar, fullName, phoneNumber, contactEmail, addressModel);
        this.setLastUpdate(lastUpdate);
    }

    public AddressModel getAddress() {
        return address;
    }

    public void setAddress(AddressModel address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
