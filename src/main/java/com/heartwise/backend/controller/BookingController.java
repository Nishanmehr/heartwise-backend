package com.heartwise.backend.controller;

import com.heartwise.backend.dto.BookingRequest;
import com.heartwise.backend.entity.Booking;
import com.heartwise.backend.entity.Mentor;
import com.heartwise.backend.entity.Notification;
import com.heartwise.backend.entity.User;
import com.heartwise.backend.repository.BookingRepository;
import com.heartwise.backend.repository.MentorRepository;
import com.heartwise.backend.repository.NotificationRepository;
import com.heartwise.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/sessions")
public class BookingController {

    @Autowired private BookingRepository      bookingRepository;
    @Autowired private UserRepository         userRepository;
    @Autowired private MentorRepository       mentorRepository;
    @Autowired private NotificationRepository notificationRepository;

    /* ──────────────────────────────────────────
       POST /api/sessions/book
       Header: X-User-Id: <userId>
       Body:   { mentorId, type, slot }
    ────────────────────────────────────────── */
    @PostMapping("/book")
    public ResponseEntity<?> bookSession(
            @RequestHeader("X-User-Id") Integer userId,
            @RequestBody BookingRequest request) {

        Optional<User>   userOpt   = userRepository.findById(userId);
        Optional<Mentor> mentorOpt = mentorRepository.findById(request.getMentorId());

        if (userOpt.isEmpty())
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        if (mentorOpt.isEmpty())
            return ResponseEntity.status(404).body(Map.of("message", "Mentor not found"));

        Booking booking = new Booking();
        booking.setUser(userOpt.get());
        booking.setMentor(mentorOpt.get());
        // Strip emojis to avoid MySQL charset issues
        String sessionType = request.getType() != null
                ? request.getType().replaceAll("[^\\x00-\\x7F]", "").trim() : "Chat";
        String slot = request.getSlot() != null
                ? request.getSlot().replaceAll("[^\\x00-\\x7F\\u0900-\\u097F\\s:,.-]", "").trim() : "";
        booking.setSessionType(sessionType);
        booking.setSlot(slot);
        booking.setStatus("PENDING");
        bookingRepository.save(booking);

        return ResponseEntity.ok(Map.of(
                "message",   "Booking request sent successfully",
                "bookingId", booking.getId(),
                "status",    "PENDING"
        ));
    }

    /* ──────────────────────────────────────────
       GET /api/sessions/mentor/:mentorId
       Returns all PENDING bookings for dashboard
    ────────────────────────────────────────── */
    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<List<Map<String, Object>>> getMentorSessions(@PathVariable int mentorId) {
        List<Booking> bookings = bookingRepository.findByMentor_Id(mentorId);
        List<Map<String, Object>> response = new ArrayList<>();

        for (Booking b : bookings) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id",          b.getId());
            map.put("status",      b.getStatus());
            map.put("sessionType", b.getSessionType());
            map.put("slot",        b.getSlot());
            map.put("userName",    b.getUser().getName());
            map.put("userId",      b.getUser().getId());
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    /* ──────────────────────────────────────────
       GET /api/sessions/user/:userId
       Returns all bookings for a user
    ────────────────────────────────────────── */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getUserSessions(@PathVariable int userId) {
        List<Booking> bookings = bookingRepository.findByUser_Id(userId);
        List<Map<String, Object>> response = new ArrayList<>();

        for (Booking b : bookings) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id",          b.getId());
            map.put("status",      b.getStatus());
            map.put("sessionType", b.getSessionType());
            map.put("slot",        b.getSlot());
            map.put("mentorName",  b.getMentor().getName());
            map.put("mentorId",    b.getMentor().getId());
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    /* ──────────────────────────────────────────
       PUT /api/sessions/:id/accept
       → sets status ACCEPTED
       → creates notification for the user
    ────────────────────────────────────────── */
    @PutMapping("/{id}/accept")
    public ResponseEntity<?> acceptSession(@PathVariable Integer id) {
        Optional<Booking> opt = bookingRepository.findById(id);
        if (opt.isEmpty())
            return ResponseEntity.status(404).body(Map.of("message", "Booking not found"));

        Booking booking = opt.get();
        booking.setStatus("ACCEPTED");
        bookingRepository.save(booking);

        // Create notification for the user
        Notification notif = new Notification();
        notif.setUser(booking.getUser());
        notif.setType("ACCEPTED");
        notif.setMessage(
                booking.getMentor().getName() +
                        " accepted your booking for " +
                        booking.getSlot() + " 🎉"
        );
        notif.setRead(false);
        notificationRepository.save(notif);

        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(Map.of("message", "Session accepted", "status", "ACCEPTED"));
    }

    /* ──────────────────────────────────────────
       PUT /api/sessions/:id/decline
       → sets status DECLINED
       → creates notification for the user
    ────────────────────────────────────────── */
    @PutMapping("/{id}/decline")
    public ResponseEntity<?> declineSession(@PathVariable Integer id) {
        Optional<Booking> opt = bookingRepository.findById(id);
        if (opt.isEmpty())
            return ResponseEntity.status(404).body(Map.of("message", "Booking not found"));

        Booking booking = opt.get();
        booking.setStatus("DECLINED");
        bookingRepository.save(booking);

        // Create notification for the user
        Notification notif = new Notification();
        notif.setUser(booking.getUser());
        notif.setType("DECLINED");
        notif.setMessage(
                booking.getMentor().getName() +
                        " is unavailable for " +
                        booking.getSlot() + ". Please pick another slot. 🙏"
        );
        notif.setRead(false);
        notificationRepository.save(notif);

        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(Map.of("message", "Session declined", "status", "DECLINED"));
    }

    /* ──────────────────────────────────────────
       GET /api/sessions/notifications/:userId
       Returns all notifications for a user
    ────────────────────────────────────────── */
    @GetMapping("/notifications/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getNotifications(@PathVariable int userId) {
        List<Notification> notifs = notificationRepository.findByUser_IdOrderByIdDesc(userId);
        List<Map<String, Object>> response = new ArrayList<>();

        for (Notification n : notifs) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id",      n.getId());
            map.put("message", n.getMessage());
            map.put("type",    n.getType());
            map.put("read",    n.isRead());
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    /* ──────────────────────────────────────────
       PUT /api/sessions/notifications/:id/read
       Mark a notification as read
    ────────────────────────────────────────── */
    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable Integer id) {
        Optional<Notification> opt = notificationRepository.findById(id);
        if (opt.isEmpty())
            return ResponseEntity.status(404).body(Map.of("message", "Notification not found"));

        Notification notif = opt.get();
        notif.setRead(true);
        notificationRepository.save(notif);
        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(Map.of("message", "Marked as read"));
    }
}