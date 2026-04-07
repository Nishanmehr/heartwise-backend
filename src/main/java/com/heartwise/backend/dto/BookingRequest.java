package com.heartwise.backend.dto;

public class BookingRequest {

    private Integer mentorId;
    private String  type;   // "CHAT" or "CALL"
    private String  slot;   // e.g. "Today 4 PM"

    public BookingRequest() {}

    public Integer getMentorId() { return mentorId; }
    public void setMentorId(Integer mentorId) { this.mentorId = mentorId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSlot() { return slot; }
    public void setSlot(String slot) { this.slot = slot; }
}