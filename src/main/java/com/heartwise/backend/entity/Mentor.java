package com.heartwise.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "mentors")
@Data
public class Mentor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String  firstName;
    private String  lastName;
    private String  email;
    private String  password;
    private String  specialty;
    private String  skills;
    private String  languages;
    private String  gender;
    private String  dob;
    private Integer experience = 0;
    private double  price      = 500.0;
    private double  rating     = 5.0;

    @Column(columnDefinition = "TEXT")
    private String profilePicture;

    // Admin approval
    private boolean approved = false;

    public String getName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
}