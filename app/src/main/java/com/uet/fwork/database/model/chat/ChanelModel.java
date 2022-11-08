package com.uet.fwork.database.model.chat;

import java.util.Map;

public class ChanelModel {
    private String id;
    private Long lastUpdate = 0L;
    private Map<String, String> members;

    public ChanelModel() {
    }

    public ChanelModel(String id, Long lastUpdate, Map<String, String> members) {
        this.id = id;
        this.lastUpdate = lastUpdate;
        this.members = members;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Map<String, String> getMembers() {
        return members;
    }

    public void setMembers(Map<String, String> members) {
        this.members = members;
    }
}
