package com.example.forum.models.requests;

public class NewCommentRequest {
    private String body;

    public NewCommentRequest(String body) {
        this.body = body;
    }

    public NewCommentRequest(){};

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
