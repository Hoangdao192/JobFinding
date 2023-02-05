package com.vnsoftware.jobfinder.database.model.chat;

/**
 * Biểu diễn các nội dung có trong một Message
 * Ví dụ: Một message có nội dung bao gồm một ảnh, một đoạn văn bản
 * thì chúng đều được coi là một MessageContent
 */
public class MessageContentModel {
    private String id;
    private String type;
    private String content;

    public MessageContentModel() {
    }

    public MessageContentModel(String id, String type, String content) {
        this.id = id;
        this.type = type;
        this.content = content;
    }

    public MessageContentModel(String type, String content) {
        this.type = type;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "MessageContentModel{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
