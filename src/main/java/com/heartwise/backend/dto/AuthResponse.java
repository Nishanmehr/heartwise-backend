package com.heartwise.backend.dto;

public class AuthResponse {

    private Integer id;
    private String  name;
    private String  email;
    private String  role;
    private String  token;  // ← was missing; frontend always reads data.token

    public AuthResponse(Integer id, String name, String email, String role, String token) {
        this.id    = id;
        this.name  = name;
        this.email = email;
        this.role  = role;
        this.token = token;
    }

    public Integer getId()    { return id; }
    public String  getName()  { return name; }
    public String  getEmail() { return email; }
    public String  getRole()  { return role; }
    public String  getToken() { return token; }
}