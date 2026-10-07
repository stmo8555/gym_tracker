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

    // nullable: NULL for a session started outside a program, or set to NULL by
    // the DB if the program is deleted, so history survives
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = true)
    private Program program;

    // nullable: set to NULL by the DB if the workout is deleted later
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_id", nullable = true)
    private Workout workout;

    // position in the program when the session was started; NULL without a program
    @Column(nullable = true)
    private Integer dayOrder;

    // Instant maps to TIMESTAMPTZ; convert to the user's zone when deciding what "today" is
    @Column(nullable = false)
    private Instant startedAt;

    // NULL while the session is in progress
    @Column(nullable = true)
    private Instant finishedAt;

    protected WorkoutSession() {} // JPA needs a no-arg constructor

    public WorkoutSession(User user, Program program, Workout workout, Integer dayOrder, Instant startedAt) {
        this.user = user;
        this.program = program;
        this.workout = workout;
        this.dayOrder = dayOrder;
        this.startedAt = startedAt;
    }
}
