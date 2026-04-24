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


    @PutMapping("/{id}")
    public ResponseEntity<?> updateMentor(@PathVariable Integer id, @RequestBody Map<String, Object> updates) {
        Optional<Mentor> optional = mentorRepository.findById(id);
        if (optional.isEmpty())
            return ResponseEntity.status(404).body(Map.of("message", "Mentor not found"));

        Mentor m = optional.get();

        if (updates.containsKey("firstName"))      m.setFirstName((String) updates.get("firstName"));
        if (updates.containsKey("lastName"))       m.setLastName((String) updates.get("lastName"));
        if (updates.containsKey("specialty"))      m.setSpecialty((String) updates.get("specialty"));
        if (updates.containsKey("skills"))         m.setSkills((String) updates.get("skills"));
        if (updates.containsKey("languages"))      m.setLanguages((String) updates.get("languages"));
        if (updates.containsKey("dob"))            m.setDob((String) updates.get("dob"));
        if (updates.containsKey("gender"))         m.setGender((String) updates.get("gender"));
        if (updates.containsKey("profilePicture")) m.setProfilePicture((String) updates.get("profilePicture"));
        if (updates.containsKey("experience"))     m.setExperience((Integer) updates.get("experience"));
        if (updates.containsKey("price")) {
            Object p = updates.get("price");
            m.setPrice(p instanceof Integer ? ((Integer)p).doubleValue() : (Double)p);
        }

        mentorRepository.save(m);
        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(Map.of("message", "Profile updated successfully"));
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
        map.put("price",          "₹" + (double) m.getPrice());
        map.put("rating",         m.getRating());
        map.put("experience",     m.getExperience() + " yrs");
        map.put("gender",         m.getGender());
        map.put("dob",            m.getDob());
        map.put("profilePicture", m.getProfilePicture());
        return map;
    }
}