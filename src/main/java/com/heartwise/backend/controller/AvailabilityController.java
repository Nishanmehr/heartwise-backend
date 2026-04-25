package com.heartwise.backend.controller;

import com.heartwise.backend.entity.MentorAvailability;
import com.heartwise.backend.repository.MentorAvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

    @Autowired
    private MentorAvailabilityRepository availabilityRepository;

    /* ══ GET mentor availability ══ */
    @GetMapping("/{mentorId}")
    public ResponseEntity<?> getAvailability(@PathVariable Integer mentorId) {
        List<MentorAvailability> slots = availabilityRepository.findByMentorId(mentorId);
        return ResponseEntity.ok(slots);
    }

    /* ══ SAVE mentor availability ══ */
    @PostMapping("/{mentorId}")
    @Transactional
    public ResponseEntity<?> saveAvailability(
            @PathVariable Integer mentorId,
            @RequestBody List<Map<String, Object>> days) {

        // Delete existing
        availabilityRepository.deleteByMentorId(mentorId);

        // Save new
        List<MentorAvailability> saved = new ArrayList<>();
        for (Map<String, Object> day : days) {
            Boolean active = (Boolean) day.getOrDefault("active", false);
            if (!active) continue;

            MentorAvailability av = new MentorAvailability();
            av.setMentorId(mentorId);
            av.setDayOfWeek((String) day.get("dayOfWeek"));
            av.setStartTime((String) day.get("startTime"));
            av.setEndTime((String) day.get("endTime"));
            av.setSlotDuration(Integer.parseInt(day.get("slotDuration").toString()));
            av.setActive(true);
            saved.add(av);
        }

        availabilityRepository.saveAll(saved);
        return ResponseEntity.ok(Map.of("message", "Availability saved ✅"));
    }

    /* ══ GET generated slots for a mentor (for booking page) ══ */
    @GetMapping("/{mentorId}/slots")
    public ResponseEntity<?> getSlots(@PathVariable Integer mentorId) {
        List<MentorAvailability> availability = availabilityRepository.findByMentorId(mentorId);

        if (availability.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<Map<String, String>> slots = new ArrayList<>();

        // Generate slots for next 7 days
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate date = today.plusDays(i);
            String dayName = date.getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();

            // Find availability for this day
            for (MentorAvailability av : availability) {
                if (!av.getDayOfWeek().equalsIgnoreCase(dayName)) continue;

                // Generate time slots
                LocalTime start    = LocalTime.parse(av.getStartTime());
                LocalTime end      = LocalTime.parse(av.getEndTime());
                int       duration = av.getSlotDuration();

                while (start.plusMinutes(duration).compareTo(end) <= 0) {
                    String label = (i == 0 ? "Today" : i == 1 ? "Tomorrow"
                            : date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                            + " " + formatTime(start);

                    Map<String, String> slot = new LinkedHashMap<>();
                    slot.put("label",    label);
                    slot.put("value",    date + " " + start);
                    slot.put("date",     date.toString());
                    slot.put("time",     start.toString());
                    slot.put("duration", duration + " min");
                    slots.add(slot);

                    start = start.plusMinutes(duration);
                }
            }
        }

        return ResponseEntity.ok(slots);
    }

    private String formatTime(LocalTime time) {
        int hour   = time.getHour();
        int minute = time.getMinute();
        String ampm = hour >= 12 ? "PM" : "AM";
        int h12    = hour % 12 == 0 ? 12 : hour % 12;
        return h12 + (minute == 0 ? "" : ":" + String.format("%02d", minute)) + " " + ampm;
    }
}