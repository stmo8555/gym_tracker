package com.github.stmo8555.gymtracker.exercise;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRepository extends JpaRepository<Exercise, Integer> {
    List<Exercise> findByWorkoutIdOrderByPositionAsc(Integer workoutId);

    // last exercise in the workout, to append new ones after it
    Optional<Exercise> findFirstByWorkoutIdOrderByPositionDesc(Integer workoutId);
}
