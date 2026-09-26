package com.github.stmo8555.gymtracker.exercise;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRepository extends JpaRepository<Exercise, Integer> {
    List<Exercise> findByWorkoutId(Integer workoutId);
}
