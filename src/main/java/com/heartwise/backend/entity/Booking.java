package com.heartwise.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "bookings")
@Data
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "mentor_id")
    private Mentor mentor;

    // Session type: "CHAT" or "CALL"
    private String sessionType;

    // Slot string e.g. "Today 4 PM"
    private String slot;

    // Status: "PENDING", "ACCEPTED", "DECLINED"
    private String status = "PENDING";
}