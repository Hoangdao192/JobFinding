package com.uet.fwork.database.model;

import lombok.Data;

@Data
public class EmployerModel extends UserModel {
    private AddressModel addressModel;

    public EmployerModel() {
        super();
    }

    public EmployerModel(
            String id, String email,
            String avatar, String fullName, String phoneNumber,
            String contactEmail, AddressModel addressModel
    ) {
        super(id, email, avatar, fullName, phoneNumber, contactEmail, UserRole.EMPLOYER);
        this.addressModel = addressModel;
    }
}
