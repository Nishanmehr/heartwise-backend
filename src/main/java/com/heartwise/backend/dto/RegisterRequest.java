package com.heartwise.backend.dto;

public class RegisterRequest {

    private String name;
    private String email;
    private String password;

    // Mentor-only fields (null for user registration)
    private String specialty;
    private Integer experience;
    private Double  price;

    public RegisterRequest() {}

    public String  getName()       { return name; }
    public void    setName(String name) { this.name = name; }

    public String  getEmail()      { return email; }
    public void    setEmail(String email) { this.email = email; }

    public String  getPassword()   { return password; }
    public void    setPassword(String password) { this.password = password; }

    public String  getSpecialty()  { return specialty; }
    public void    setSpecialty(String specialty) { this.specialty = specialty; }

    public Integer getExperience() { return experience; }
    public void    setExperience(Integer experience) { this.experience = experience; }

    public Double  getPrice()      { return price; }
    public void    setPrice(Double price) { this.price = price; }
}