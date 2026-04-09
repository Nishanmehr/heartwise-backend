package com.heartwise.backend.controller;

import com.heartwise.backend.dto.AuthResponse;
import com.heartwise.backend.dto.LoginRequest;
import com.heartwise.backend.dto.RegisterRequest;
import com.heartwise.backend.entity.Mentor;
import com.heartwise.backend.entity.User;
import com.heartwise.backend.repository.MentorRepository;
import com.heartwise.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private UserRepository   userRepository;
    @Autowired private MentorRepository mentorRepository;

    /* ========== USER LOGIN ========== */
    @PostMapping("/user/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null || !user.getPassword().equals(request.getPassword()))
            return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));

        return ResponseEntity.ok(new AuthResponse(
                user.getId(), user.getName(), user.getEmail(), "USER", UUID.randomUUID().toString()
        ));
    }

    /* ========== USER REGISTER ========== */
    @PostMapping("/user/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        if (isBlank(request.getName()))
            return ResponseEntity.badRequest().body(Map.of("message", "Name is required"));
        if (isBlank(request.getEmail()))
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        if (isBlank(request.getPassword()) || request.getPassword().length() < 6)
            return ResponseEntity.badRequest().body(Map.of("message", "Password must be at least 6 characters"));
        if (userRepository.findByEmail(request.getEmail()) != null)
            return ResponseEntity.status(409).body(Map.of("message", "Email already registered"));

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole("USER");
        userRepository.save(user);

        return ResponseEntity.ok(new AuthResponse(
                user.getId(), user.getName(), user.getEmail(), "USER", UUID.randomUUID().toString()
        ));
    }

    /* ========== MENTOR LOGIN ========== */
    @PostMapping("/mentor/login")
    public ResponseEntity<?> loginMentor(@RequestBody LoginRequest request) {
        Mentor mentor = mentorRepository.findByEmail(request.getEmail());
        if (mentor == null || !mentor.getPassword().equals(request.getPassword()))
            return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));

        return ResponseEntity.ok(new AuthResponse(
                mentor.getId(), mentor.getName(), mentor.getEmail(), "MENTOR", UUID.randomUUID().toString()
        ));
    }

    /* ========== MENTOR REGISTER ========== */
    @PostMapping("/mentor/register")
    public ResponseEntity<?> registerMentor(@RequestBody RegisterRequest request) {
        // Validate required fields
        if (isBlank(request.getFirstName()))
            return ResponseEntity.badRequest().body(Map.of("message", "First name is required"));
        if (isBlank(request.getLastName()))
            return ResponseEntity.badRequest().body(Map.of("message", "Last name is required"));
        if (isBlank(request.getEmail()))
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        if (isBlank(request.getPassword()) || request.getPassword().length() < 6)
            return ResponseEntity.badRequest().body(Map.of("message", "Password must be at least 6 characters"));
        if (isBlank(request.getDob()))
            return ResponseEntity.badRequest().body(Map.of("message", "Date of birth is required"));
        if (isBlank(request.getGender()))
            return ResponseEntity.badRequest().body(Map.of("message", "Gender is required"));
        if (isBlank(request.getSpecialty()))
            return ResponseEntity.badRequest().body(Map.of("message", "Specialty is required"));
        if (isBlank(request.getLanguages()))
            return ResponseEntity.badRequest().body(Map.of("message", "Languages are required"));
        if (isBlank(request.getSkills()))
            return ResponseEntity.badRequest().body(Map.of("message", "Skills are required"));

        // Check duplicate email
        if (mentorRepository.findByEmail(request.getEmail()) != null)
            return ResponseEntity.status(409).body(Map.of("message", "Email already registered"));

        // Save mentor
        Mentor mentor = new Mentor();
        mentor.setFirstName(request.getFirstName());
        mentor.setLastName(request.getLastName());
        mentor.setEmail(request.getEmail());
        mentor.setPassword(request.getPassword());
        mentor.setDob(request.getDob());
        mentor.setGender(request.getGender());
        mentor.setSpecialty(request.getSpecialty());
        mentor.setSkills(request.getSkills());
        mentor.setLanguages(request.getLanguages());
        mentor.setExperience(request.getExperience() != null ? request.getExperience() : 0);
        mentor.setPrice(request.getPrice()      != null ? request.getPrice()      : 500.0);
        mentor.setProfilePicture(request.getProfilePicture());
        mentor.setRating(5.0);
        mentorRepository.save(mentor);

        return ResponseEntity.ok(new AuthResponse(
                mentor.getId(), mentor.getName(), mentor.getEmail(), "MENTOR", UUID.randomUUID().toString()
        ));
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}