package com.vnsoftware.jobfinder.database.model;

import java.io.Serializable;

public class UserDeviceModel implements Serializable {
    private String userId;
    private String deviceMessageToken;

    public UserDeviceModel() {
    }

    public UserDeviceModel(String userId, String deviceMessageToken) {
        this.userId = userId;
        this.deviceMessageToken = deviceMessageToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceMessageToken() {
        return deviceMessageToken;
    }

    public void setDeviceMessageToken(String deviceMessageToken) {
        this.deviceMessageToken = deviceMessageToken;
    }
}
