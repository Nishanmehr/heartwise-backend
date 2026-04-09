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

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllMentors() {
        List<Mentor> mentors = mentorRepository.findAll();
        List<Map<String, Object>> response = new ArrayList<>();

        for (Mentor m : mentors) {
            response.add(buildMentorMap(m));
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMentorById(@PathVariable Integer id) {
        Optional<Mentor> optional = mentorRepository.findById(id);
        if (optional.isEmpty())
            return ResponseEntity.status(404).body(Map.of("message", "Mentor not found"));

        return ResponseEntity.ok(buildMentorMap(optional.get()));
    }

    private Map<String, Object> buildMentorMap(Mentor m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",             m.getId());
        map.put("name",           m.getName());
        map.put("firstName",      m.getFirstName());
        map.put("lastName",       m.getLastName());
        map.put("specialty",      m.getSpecialty());
        map.put("skills",         m.getSkills());
        map.put("languages",      m.getLanguages());
        map.put("price",          "₹" + (int) m.getPrice());
        map.put("rating",         m.getRating());
        map.put("experience",     m.getExperience() + " yrs");
        map.put("gender",         m.getGender());
        map.put("dob",            m.getDob());
        map.put("profilePicture", m.getProfilePicture());
        return map;
    }
}