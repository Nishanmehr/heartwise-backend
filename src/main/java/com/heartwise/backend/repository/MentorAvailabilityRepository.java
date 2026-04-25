package com.heartwise.backend.repository;

import com.heartwise.backend.entity.MentorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MentorAvailabilityRepository extends JpaRepository<MentorAvailability, Integer> {
    List<MentorAvailability> findByMentorId(Integer mentorId);
    void deleteByMentorId(Integer mentorId);
}