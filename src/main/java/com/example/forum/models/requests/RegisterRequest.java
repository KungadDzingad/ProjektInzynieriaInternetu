package com.example.forum.models.requests;

public class RegisterRequest {
    private String mail;
    private String username;
    private String password;
    private String nick;

    public RegisterRequest(String mail, String username, String password, String nick) {
        this.mail = mail;
        this.username = username;
        this.password = password;
        this.nick = nick;
    }

    public String getMail() {
        return mail;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getNick() {
        return nick;
    }
}
