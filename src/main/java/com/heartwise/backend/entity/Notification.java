package com.heartwise.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "notifications")
@Data
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String message;

    private String type;

    // 'read' is reserved in MySQL — backticks tell Hibernate to escape it
    @Column(name = "`read`")
    private boolean read = false;
}