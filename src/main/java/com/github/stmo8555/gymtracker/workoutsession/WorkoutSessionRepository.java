package com.github.stmo8555.gymtracker.workoutsession;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Integer> {
    // latest session, for the home page's "done today?" check
    Optional<WorkoutSession> findFirstByUserIdOrderByCompletedAtDesc(Integer userId);
}
