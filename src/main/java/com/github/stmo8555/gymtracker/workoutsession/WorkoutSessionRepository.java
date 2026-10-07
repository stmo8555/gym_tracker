package com.github.stmo8555.gymtracker.workoutsession;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Integer> {
    // latest finished session, for the home page's "done today?" check
    Optional<WorkoutSession> findFirstByUserIdAndFinishedAtNotNullOrderByFinishedAtDesc(Integer userId);

    // the in-progress session; at most one per user (one_in_progress_session_per_user)
    Optional<WorkoutSession> findByUserIdAndFinishedAtIsNull(Integer userId);
}
