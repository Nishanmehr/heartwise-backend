package com.heartwise.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "mentor_availability")
@Data
public class MentorAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer mentorId;

    // Day: MONDAY, TUESDAY, etc.
    private String dayOfWeek;

    // e.g. "09:00"
    private String startTime;

    // e.g. "18:00"
    private String endTime;

    // 30, 60, or 90
    private Integer slotDuration;

    // Is this day active?
    private boolean active = true;
}