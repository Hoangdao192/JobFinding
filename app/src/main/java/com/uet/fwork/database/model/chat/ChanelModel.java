package com.uet.fwork.database.model.chat;

import androidx.annotation.NonNull;

import java.util.List;

public class ChanelModel {
    private @NonNull String id;
    private @NonNull Long lastUpdate = 0L;
    private @NonNull List<String> members;

    public ChanelModel() {
    }

    public ChanelModel(@NonNull String id, @NonNull Long lastUpdate, @NonNull List<String> members) {
        this.id = id;
        this.lastUpdate = lastUpdate;
        this.members = members;
    }

    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public Long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(@NonNull Long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(@NonNull List<String> members) {
        this.members = members;
    }
}
