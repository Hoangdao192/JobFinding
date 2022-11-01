package com.uet.fwork.database.model;

public class EmployerModel extends UserModel {
    private AddressModel address;

    public EmployerModel() {
        super();
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
}
