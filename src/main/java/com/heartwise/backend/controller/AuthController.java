package com.heartwise.backend.controller;

import com.heartwise.backend.dto.AuthResponse;
import com.heartwise.backend.dto.LoginRequest;
import com.heartwise.backend.dto.OtpRequest;
import com.heartwise.backend.dto.RegisterRequest;
import com.heartwise.backend.entity.Mentor;
import com.heartwise.backend.entity.User;
import com.heartwise.backend.repository.MentorRepository;
import com.heartwise.backend.repository.UserRepository;
import com.heartwise.backend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private UserRepository   userRepository;
    @Autowired private MentorRepository mentorRepository;
    @Autowired private EmailService     emailService;

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private void trySendOtp(String email, String name, String otp) {
        try {
            emailService.sendOtp(email, name, otp);
        } catch (Exception e) {
            System.err.println("OTP email failed: " + e.getMessage());
        }
    }

    /* ══════════════ USER LOGIN ══════════════ */
    @PostMapping("/user/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null || !user.getPassword().equals(request.getPassword()))
            return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));

        if (!user.isVerified())
            return ResponseEntity.status(403).body(Map.of(
                    "message", "Email not verified",
                    "needsVerification", true,
                    "email", user.getEmail()
            ));

        return ResponseEntity.ok(new AuthResponse(
                user.getId(), user.getName(), user.getEmail(), "USER",
                UUID.randomUUID().toString()
        ));
    }

    /* ══════════════ USER REGISTER ══════════════ */
    @PostMapping("/user/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        if (isBlank(request.getName()))
            return ResponseEntity.badRequest().body(Map.of("message", "Name is required"));
        if (isBlank(request.getEmail()))
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        if (isBlank(request.getPassword()) || request.getPassword().length() < 6)
            return ResponseEntity.badRequest().body(Map.of("message", "Password must be at least 6 characters"));

        User existing = userRepository.findByEmail(request.getEmail());
        if (existing != null) {
            if (!existing.isVerified()) {
                String otp = generateOtp();
                existing.setOtp(otp);
                userRepository.save(existing);
                trySendOtp(existing.getEmail(), existing.getName(), otp);
                return ResponseEntity.ok(Map.of(
                        "message", "OTP resent",
                        "needsVerification", true,
                        "email", existing.getEmail()
                ));
            }
            return ResponseEntity.status(409).body(Map.of("message", "Email already registered"));
        }

        String otp = generateOtp();
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole("USER");
        user.setOtp(otp);
        user.setVerified(false);
        userRepository.save(user);

        trySendOtp(user.getEmail(), user.getName(), otp);

        return ResponseEntity.ok(Map.of(
                "message", "OTP sent to " + user.getEmail(),
                "needsVerification", true,
                "email", user.getEmail()
        ));
    }

    /* ══════════════ VERIFY OTP ══════════════ */
    @PostMapping("/user/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null)
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        if (!request.getOtp().equals(user.getOtp()))
            return ResponseEntity.status(400).body(Map.of("message", "Invalid OTP ❌"));

        user.setVerified(true);
        user.setOtp(null);
        userRepository.save(user);

        return ResponseEntity.ok(new AuthResponse(
                user.getId(), user.getName(), user.getEmail(), "USER",
                UUID.randomUUID().toString()
        ));
    }

    /* ══════════════ RESEND OTP ══════════════ */
    @PostMapping("/user/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        User user = userRepository.findByEmail(email);
        if (user == null)
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        if (user.isVerified())
            return ResponseEntity.badRequest().body(Map.of("message", "Email already verified"));

        String otp = generateOtp();
        user.setOtp(otp);
        userRepository.save(user);
        trySendOtp(user.getEmail(), user.getName(), otp);

        return ResponseEntity.ok(Map.of("message", "OTP resent to " + email));
    }

    /* ══════════════ MENTOR LOGIN ══════════════ */
    @PostMapping("/mentor/login")
    public ResponseEntity<?> loginMentor(@RequestBody LoginRequest request) {
        Mentor mentor = mentorRepository.findByEmail(request.getEmail());
        if (mentor == null || !mentor.getPassword().equals(request.getPassword()))
            return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));

        // Check email verified
        if (!mentor.isVerified())
            return ResponseEntity.status(403).body(Map.of(
                    "message", "Email not verified",
                    "needsVerification", true,
                    "email", mentor.getEmail()
            ));

        // Check admin approved
        if (!mentor.isApproved())
            return ResponseEntity.status(403).body(Map.of(
                    "message", "Your application is under review.",
                    "pendingApproval", true,
                    "email", mentor.getEmail(),
                    "name", mentor.getName()
            ));

        return ResponseEntity.ok(new AuthResponse(
                mentor.getId(), mentor.getName(), mentor.getEmail(), "MENTOR",
                UUID.randomUUID().toString()
        ));
    }

    /* ══════════════ MENTOR REGISTER ══════════════ */
    @PostMapping("/mentor/register")
    public ResponseEntity<?> registerMentor(@RequestBody RegisterRequest request) {
        if (isBlank(request.getFirstName()))
            return ResponseEntity.badRequest().body(Map.of("message", "First name is required"));
        if (isBlank(request.getLastName()))
            return ResponseEntity.badRequest().body(Map.of("message", "Last name is required"));
        if (isBlank(request.getEmail()))
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        if (isBlank(request.getPassword()) || request.getPassword().length() < 6)
            return ResponseEntity.badRequest().body(Map.of("message", "Password must be at least 6 characters"));
        if (isBlank(request.getSpecialty()))
            return ResponseEntity.badRequest().body(Map.of("message", "Specialty is required"));
        if (isBlank(request.getLanguages()))
            return ResponseEntity.badRequest().body(Map.of("message", "Languages are required"));
        if (isBlank(request.getSkills()))
            return ResponseEntity.badRequest().body(Map.of("message", "Skills are required"));
        if (isBlank(request.getProfilePicture()))
            return ResponseEntity.badRequest().body(Map.of("message", "Profile picture is required"));

        // Check duplicate
        Mentor existing = mentorRepository.findByEmail(request.getEmail());
        if (existing != null) {
            if (!existing.isVerified()) {
                String otp = generateOtp();
                existing.setOtp(otp);
                mentorRepository.save(existing);
                trySendOtp(existing.getEmail(), existing.getName(), otp);
                return ResponseEntity.ok(Map.of(
                        "message", "OTP resent",
                        "needsVerification", true,
                        "email", existing.getEmail()
                ));
            }
            return ResponseEntity.status(409).body(Map.of("message", "Email already registered"));
        }

        String otp = generateOtp();
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
        mentor.setOtp(otp);
        mentor.setVerified(false);
        mentor.setApproved(false);
        mentorRepository.save(mentor);

        trySendOtp(mentor.getEmail(), mentor.getName(), otp);

        return ResponseEntity.ok(Map.of(
                "message", "OTP sent to " + mentor.getEmail(),
                "needsVerification", true,
                "email", mentor.getEmail()
        ));
    }

    /* ══════════════ MENTOR VERIFY OTP ══════════════ */
    @PostMapping("/mentor/verify-otp")
    public ResponseEntity<?> verifyMentorOtp(@RequestBody OtpRequest request) {
        Mentor mentor = mentorRepository.findByEmail(request.getEmail());
        if (mentor == null)
            return ResponseEntity.status(404).body(Map.of("message", "Mentor not found"));
        if (!request.getOtp().equals(mentor.getOtp()))
            return ResponseEntity.status(400).body(Map.of("message", "Invalid OTP ❌"));

        mentor.setVerified(true);
        mentor.setOtp(null);
        mentorRepository.save(mentor);

        // Return pending approval status
        return ResponseEntity.ok(Map.of(
                "message", "Email verified! Your application is under review.",
                "pendingApproval", true,
                "email", mentor.getEmail(),
                "name", mentor.getName()
        ));
    }

    /* ══════════════ MENTOR RESEND OTP ══════════════ */
    @PostMapping("/mentor/resend-otp")
    public ResponseEntity<?> resendMentorOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        Mentor mentor = mentorRepository.findByEmail(email);
        if (mentor == null)
            return ResponseEntity.status(404).body(Map.of("message", "Mentor not found"));
        if (mentor.isVerified())
            return ResponseEntity.badRequest().body(Map.of("message", "Email already verified"));

        String otp = generateOtp();
        mentor.setOtp(otp);
        mentorRepository.save(mentor);
        trySendOtp(mentor.getEmail(), mentor.getName(), otp);

        return ResponseEntity.ok(Map.of("message", "OTP resent to " + email));
    }


    /* ══════════════ MENTOR STATUS ══════════════ */
    @GetMapping("/mentor/status")
    public ResponseEntity<?> mentorStatus(@RequestParam String email) {
        Mentor mentor = mentorRepository.findByEmail(email);
        if (mentor == null)
            return ResponseEntity.status(404).body(Map.of("message", "Mentor not found"));
        return ResponseEntity.ok(Map.of(
                "approved", mentor.isApproved(),
                "verified", mentor.isVerified(),
                "name",     mentor.getName()
        ));
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}