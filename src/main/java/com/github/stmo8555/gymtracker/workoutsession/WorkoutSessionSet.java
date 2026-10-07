package com.github.stmo8555.gymtracker.workoutsession;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.github.stmo8555.gymtracker.exercise.Exercise;

@Entity
@Table(name = "workout_sessions_sets")
@Getter
@Setter
public class WorkoutSessionSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_sessions_id", nullable = false)
    private WorkoutSession workoutSession;

    // links sets of the same exercise across sessions (progress over time);
    // set to NULL by the DB if the exercise is deleted
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = true)
    private Exercise exercise;

    // copied from the Exercise at the time of the session, so renaming or deleting
    // the exercise later doesn't rewrite history
    @Column(nullable = false)
    private String exerciseName;

    // 0-based like every other position; templates show setNumber + 1 ("set 1, set 2, ...")
    @Column(nullable = false)
    private Integer setNumber;

    private Integer reps;

    // BigDecimal, not double, so 82.5 kg stays exactly 82.5; null for bodyweight sets
    @Column(nullable = true, precision = 6, scale = 2)
    private BigDecimal weight;

    @Column(nullable = true)
    private String note;

    @Column(nullable = true)
    private Instant finishedAt;

    protected WorkoutSessionSet() {} // JPA needs a no-arg constructor

    public WorkoutSessionSet(WorkoutSession workoutSession, Exercise exercise, String exerciseName, Integer setNumber,
            Integer reps, BigDecimal weight, String note) {
        this.workoutSession = workoutSession;
        this.exercise = exercise;
        this.exerciseName = exerciseName;
        this.setNumber = setNumber;
        this.reps = reps;
        this.weight = weight;
        this.note = note;
    }
}
