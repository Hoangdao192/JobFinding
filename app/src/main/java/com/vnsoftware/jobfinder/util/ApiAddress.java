package com.vnsoftware.jobfinder.util;

import java.io.Serializable;

public class ApiAddress implements Serializable {
    private String id = "";
    private String placeName = "";
    private String fullAddress = "";
    private double latitude = -1d, longitude = -1d;

    public ApiAddress(String id, String fullAddress) {
        this.id = id;
        this.fullAddress = fullAddress;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ApiAddress(String id, String fullAddress, double latitude, double longitude) {
        this.id = id;
        this.fullAddress = fullAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public ApiAddress(String fullAddress, double latitude, double longitude) {
        this.fullAddress = fullAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public ApiAddress(String id, String placeName, String fullAddress, double latitude, double longitude) {
        this.id = id;
        this.placeName = placeName;
        this.fullAddress = fullAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public ApiAddress() {
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

    @Override
    public String toString() {
        return "ApiAddress{" +
                "id='" + id + '\'' +
                ", fullAddress='" + fullAddress + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}