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

    // Basic Info
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String dob;          // Date of Birth e.g. "1990-05-15"
    private String gender;       // "Male", "Female", "Other"

    // Professional Info
    private String specialty;    // Main expertise
    private String skills;       // Comma separated e.g. "Breakup Recovery, Anxiety, Trust"
    private String languages;    // Comma separated e.g. "Hindi, English"
    private int    experience;   // Years
    private double price;        // Per 30 min in ₹
    private double rating;       // e.g. 4.9

    // Profile Picture — stored as URL or base64
    @Column(columnDefinition = "TEXT")
    private String profilePicture;

    // Helper method to get full name
    public String getName() {
        return (firstName != null ? firstName : "") +
                (lastName  != null ? " " + lastName : "");
    }
}