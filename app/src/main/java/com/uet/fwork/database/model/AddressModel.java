package com.uet.fwork.database.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressModel {
    private String province;
    private String district;
    private String ward;
    private String detailAddress;

    @Override
    public String toString() {
        return detailAddress + ", " + ward + ", " + district + ", " + province;
    }
}
