package com.github.stmo8555.gymtracker.workoutsession;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutSessionSetRepository extends JpaRepository<WorkoutSessionSet, Integer> {
    List<WorkoutSessionSet> findByWorkoutSessionIdOrderByIdAsc(Integer workoutSessionId);
}
