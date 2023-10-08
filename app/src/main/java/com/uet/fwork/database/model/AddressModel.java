package com.uet.fwork.database.model;

import java.io.Serializable;

public class AddressModel implements Serializable {
    private String province = "";
    private String district = "";
    private String ward = "";
    private String detailAddress = "";
    private String fullAddress = "";
    private double latitude = -1d, longitude = -1d;

    public AddressModel() {
    }

    public AddressModel(String province, String district, String ward, String detailAddress) {
        this.province = province;
        this.district = district;
        this.ward = ward;
        this.detailAddress = detailAddress;
    }

    public AddressModel(
            String province, String district, String ward,
            String detailAddress, String fullAddress,
            double latitude, double longitude) {
        this.province = province;
        this.district = district;
        this.ward = ward;
        this.detailAddress = detailAddress;
        this.fullAddress = fullAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
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
