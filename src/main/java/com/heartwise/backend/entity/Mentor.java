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

    private String name;
    private String email;
    private String password;

    // Renamed from 'expertise' → 'specialty' to match frontend expectation
    private String specialty;

    private int experience;   // years
    private double price;     // per 30 min in ₹
    private double rating;    // e.g. 4.9
}