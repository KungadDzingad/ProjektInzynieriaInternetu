package com.example.forum.models.requests;

public class CreateNewTopicRequest {
    private String head;
    private String category;
    private String body;

    public CreateNewTopicRequest(String head, String category, String body) {
        this.head = head;
        this.category = category;
        this.body = body;
    }

    public String getHead() {
        return head;
    }

    public String getCategory() {
        return category;
    }

    public String getBody() {
        return body;
    }
}
