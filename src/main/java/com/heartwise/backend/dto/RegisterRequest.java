package com.heartwise.backend.dto;

public class RegisterRequest {

    // User fields
    private String name;

    // Mentor-specific fields
    private String firstName;
    private String lastName;
    private String dob;
    private String gender;
    private String languages;
    private String skills;
    private String profilePicture;

    // Shared fields
    private String  email;
    private String  password;
    private String  specialty;
    private Integer experience;
    private Double  price;

    public RegisterRequest() {}

    public String  getName()           { return name; }
    public void    setName(String v)   { this.name = v; }

    public String  getFirstName()              { return firstName; }
    public void    setFirstName(String v)      { this.firstName = v; }

    public String  getLastName()               { return lastName; }
    public void    setLastName(String v)       { this.lastName = v; }

    public String  getDob()                    { return dob; }
    public void    setDob(String v)            { this.dob = v; }

    public String  getGender()                 { return gender; }
    public void    setGender(String v)         { this.gender = v; }

    public String  getLanguages()              { return languages; }
    public void    setLanguages(String v)      { this.languages = v; }

    public String  getSkills()                 { return skills; }
    public void    setSkills(String v)         { this.skills = v; }

    public String  getProfilePicture()         { return profilePicture; }
    public void    setProfilePicture(String v) { this.profilePicture = v; }

    public String  getEmail()                  { return email; }
    public void    setEmail(String v)          { this.email = v; }

    public String  getPassword()               { return password; }
    public void    setPassword(String v)       { this.password = v; }

    public String  getSpecialty()              { return specialty; }
    public void    setSpecialty(String v)      { this.specialty = v; }

    public Integer getExperience()             { return experience; }
    public void    setExperience(Integer v)    { this.experience = v; }

    public Double  getPrice()                  { return price; }
    public void    setPrice(Double v)          { this.price = v; }
}