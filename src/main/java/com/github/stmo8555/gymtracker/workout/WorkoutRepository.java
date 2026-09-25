package com.github.stmo8555.gymtracker.workout;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutRepository extends JpaRepository<Workout, Integer> {
    // TODO: Disable open-in-view and use entity graphs / fetch joins for lazy associations used in templates
}
