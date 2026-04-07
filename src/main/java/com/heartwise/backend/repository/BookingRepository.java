package com.heartwise.backend.repository;

import com.heartwise.backend.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByUser_Id(int userId);
    List<Booking> findByMentor_Id(int mentorId);
}