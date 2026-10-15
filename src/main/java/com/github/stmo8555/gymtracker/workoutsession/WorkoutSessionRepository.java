package com.github.stmo8555.gymtracker.workoutsession;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Integer> {
    // latest finished session, for the home page's "done today?" check
    Optional<WorkoutSession> findFirstByUserIdAndFinishedAtNotNullOrderByFinishedAtDesc(Integer userId);

    // latest finished session of a workout, for prefilling reps/weight
    @Query("""
            select s from WorkoutSession s
            where s.user.id = :userId and s.workout.id = :workoutId and s.finishedAt is not null
            order by s.finishedAt desc
            limit 1""")
    Optional<WorkoutSession> findLastFinished(Integer userId, Integer workoutId);

    // the in-progress session; at most one per user (one_in_progress_session_per_user)
    Optional<WorkoutSession> findByUserIdAndFinishedAtIsNull(Integer userId);
}
