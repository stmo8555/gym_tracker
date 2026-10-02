package com.github.stmo8555.gymtracker.workoutsession;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.github.stmo8555.gymtracker.program.Program;
import com.github.stmo8555.gymtracker.user.User;
import com.github.stmo8555.gymtracker.workout.Workout;

@Entity
@Table(name = "workout_sessions")
@Getter
@Setter
public class WorkoutSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // nullable: set to NULL by the DB if the program is deleted, so history survives
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = true)
    private Program program;

    // nullable: NULL for a rest day, or if the workout is deleted later
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_id", nullable = true)
    private Workout workout;

    @Column(nullable = true)
    private Integer dayOrder;

    // Instant maps to TIMESTAMPTZ; convert to the user's zone when deciding what "today" is
    @Column(nullable = false)
    private Instant completedAt;

    protected WorkoutSession() {} // JPA needs a no-arg constructor

    public WorkoutSession(User user, Program program, Workout workout, Integer dayOrder, Instant completedAt) {
        this.user = user;
        this.program = program;
        this.workout = workout;
        this.dayOrder = dayOrder;
        this.completedAt = completedAt;
    }
}
