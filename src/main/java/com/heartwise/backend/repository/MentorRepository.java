package com.heartwise.backend.repository;

import com.heartwise.backend.entity.Mentor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorRepository extends JpaRepository<Mentor, Integer> {
    Mentor findByEmail(String email);
}