package com.example.forum.models.requests;

import java.io.Serializable;

public class ChangePasswordRequest implements Serializable {
    private String password;

    public ChangePasswordRequest() {
    }

    public ChangePasswordRequest(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}
