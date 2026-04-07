package com.heartwise.backend.dto;

public class LoginRequest {

    private String email;
    private String password;
    private String mentorId;  // Only used for mentor login validation

    public LoginRequest() {}

    public String getEmail()    { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword()    { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getMentorId()    { return mentorId; }
    public void setMentorId(String mentorId) { this.mentorId = mentorId; }
}