package com.heartwise.backend.repository;

import com.heartwise.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    // Get all notifications for a user (newest first)
    List<Notification> findByUser_IdOrderByIdDesc(int userId);

    // Get only unread notifications
    List<Notification> findByUser_IdAndReadFalse(int userId);
}