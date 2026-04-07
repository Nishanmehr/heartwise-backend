package com.heartwise.backend.controller;

import com.heartwise.backend.entity.Mentor;
import com.heartwise.backend.repository.MentorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/mentors")
public class MentorController {

    @Autowired private MentorRepository mentorRepository;

    /**
     * GET /api/mentors
     * Returns all mentors (id, name, specialty, price, rating, experience)
     * Password is intentionally excluded from response
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllMentors() {
        List<Mentor> mentors = mentorRepository.findAll();
        List<Map<String, Object>> response = new ArrayList<>();

        for (Mentor m : mentors) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id",          m.getId());
            map.put("name",        m.getName());
            map.put("specialty",   m.getSpecialty());
            map.put("price",       "₹" + (int) m.getPrice());
            map.put("rating",      m.getRating());
            map.put("experience",  m.getExperience() + " yrs");
            response.add(map);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/mentors/:id
     * Returns a single mentor by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMentorById(@PathVariable Integer id) {
        Optional<Mentor> optional = mentorRepository.findById(id);

        if (optional.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Mentor not found"));
        }

        Mentor m = optional.get();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",          m.getId());
        map.put("name",        m.getName());
        map.put("specialty",   m.getSpecialty());
        map.put("price",       "₹" + (int) m.getPrice());
        map.put("rating",      m.getRating());
        map.put("experience",  m.getExperience() + " yrs");

        return ResponseEntity.ok(map);
    }
}