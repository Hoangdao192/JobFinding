package com.uet.fwork.database.model;

import java.io.Serializable;

public class AddressModel implements Serializable {
    private String province;
    private String district;
    private String ward;
    private String detailAddress;

    public AddressModel() {
    }

    public AddressModel(String province, String district, String ward, String detailAddress) {
        this.province = province;
        this.district = district;
        this.ward = ward;
        this.detailAddress = detailAddress;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getDetailAddress() {
        return detailAddress;
    }

    public void setDetailAddress(String detailAddress) {
        this.detailAddress = detailAddress;
    }

    @Override
    public String toString() {
        return detailAddress + ", " + ward + ", " + district + ", " + province;
    }
}
