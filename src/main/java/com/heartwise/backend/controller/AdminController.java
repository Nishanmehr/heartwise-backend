package com.heartwise.backend.controller;

import com.heartwise.backend.entity.Booking;
import com.heartwise.backend.entity.Mentor;
import com.heartwise.backend.entity.User;
import com.heartwise.backend.repository.BookingRepository;
import com.heartwise.backend.repository.MentorRepository;
import com.heartwise.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private UserRepository    userRepository;
    @Autowired private MentorRepository  mentorRepository;
    @Autowired private BookingRepository bookingRepository;

    // Hardcoded admin credentials
    @Value("${ADMIN_EMAIL:admin@heartwise.com}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:heartwise@admin123}")
    private String adminPassword;

    /* ══ ADMIN LOGIN ══ */
    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> body) {
        String email    = body.getOrDefault("email", "");
        String password = body.getOrDefault("password", "");

        if (!email.equals(adminEmail) || !password.equals(adminPassword))
            return ResponseEntity.status(401).body(Map.of("message", "Invalid admin credentials"));

        return ResponseEntity.ok(Map.of(
                "token", "admin-secret-token-heartwise",
                "role",  "ADMIN",
                "name",  "Admin"
        ));
    }

    /* ══ STATS ══ */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats(@RequestHeader("Admin-Token") String token) {
        if (!validateToken(token)) return unauthorized();

        long totalUsers    = userRepository.count();
        long totalMentors  = mentorRepository.count();
        long totalBookings = bookingRepository.count();
        long pending       = bookingRepository.countByStatus("PENDING");
        long accepted      = bookingRepository.countByStatus("ACCEPTED");
        long declined      = bookingRepository.countByStatus("DECLINED");

        return ResponseEntity.ok(Map.of(
                "totalUsers",    totalUsers,
                "totalMentors",  totalMentors,
                "totalBookings", totalBookings,
                "pending",       pending,
                "accepted",      accepted,
                "declined",      declined
        ));
    }

    /* ══ GET ALL USERS ══ */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestHeader("Admin-Token") String token) {
        if (!validateToken(token)) return unauthorized();
        return ResponseEntity.ok(userRepository.findAll());
    }

    /* ══ DELETE USER ══ */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@RequestHeader("Admin-Token") String token,
                                        @PathVariable Integer id) {
        if (!validateToken(token)) return unauthorized();
        if (!userRepository.existsById(id))
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    /* ══ GET ALL MENTORS ══ */
    @GetMapping("/mentors")
    public ResponseEntity<?> getAllMentors(@RequestHeader("Admin-Token") String token) {
        if (!validateToken(token)) return unauthorized();
        return ResponseEntity.ok(mentorRepository.findAll());
    }

    /* ══ DELETE MENTOR ══ */
    @DeleteMapping("/mentors/{id}")
    public ResponseEntity<?> deleteMentor(@RequestHeader("Admin-Token") String token,
                                          @PathVariable Integer id) {
        if (!validateToken(token)) return unauthorized();
        if (!mentorRepository.existsById(id))
            return ResponseEntity.status(404).body(Map.of("message", "Mentor not found"));
        mentorRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Mentor deleted successfully"));
    }

    /* ══ GET ALL BOOKINGS ══ */
    @GetMapping("/bookings")
    public ResponseEntity<?> getAllBookings(@RequestHeader("Admin-Token") String token) {
        if (!validateToken(token)) return unauthorized();
        return ResponseEntity.ok(bookingRepository.findAll());
    }

    /* ══ DELETE BOOKING ══ */
    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<?> deleteBooking(@RequestHeader("Admin-Token") String token,
                                           @PathVariable Integer id) {
        if (!validateToken(token)) return unauthorized();
        if (!bookingRepository.existsById(id))
            return ResponseEntity.status(404).body(Map.of("message", "Booking not found"));
        bookingRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Booking deleted successfully"));
    }


    /* ══ APPROVE MENTOR ══ */
    @PutMapping("/mentors/{id}/approve")
    public ResponseEntity<?> approveMentor(@RequestHeader("Admin-Token") String token,
                                           @PathVariable Integer id) {
        if (!validateToken(token)) return unauthorized();
        return mentorRepository.findById(id).map(mentor -> {
            mentor.setApproved(true);
            mentorRepository.save(mentor);
            return ResponseEntity.ok(Map.of("message", "Mentor approved ✅"));
        }).orElse(ResponseEntity.status(404).body(Map.of("message", "Mentor not found")));
    }

    /* ══ REJECT MENTOR ══ */
    @PutMapping("/mentors/{id}/reject")
    public ResponseEntity<?> rejectMentor(@RequestHeader("Admin-Token") String token,
                                          @PathVariable Integer id) {
        if (!validateToken(token)) return unauthorized();
        return mentorRepository.findById(id).map(mentor -> {
            mentor.setApproved(false);
            mentorRepository.save(mentor);
            return ResponseEntity.ok(Map.of("message", "Mentor rejected ❌"));
        }).orElse(ResponseEntity.status(404).body(Map.of("message", "Mentor not found")));
    }

    /* ══ HELPERS ══ */
    private boolean validateToken(String token) {
        return "admin-secret-token-heartwise".equals(token);
    }

    private ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
    }
}