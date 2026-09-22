package com.github.stmo8555.gymtracker.exercise;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.github.stmo8555.gymtracker.workout.Workout;

@Entity
@Table(name = "exercises")
@Getter
@Setter
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer repRangeLower;

    @Column(nullable = false)
    private Integer repRangeUpper;

    @Column(nullable = false)
    private Integer sets;

    @Column(nullable = false)
    private Integer restSeconds;

    @Column(nullable = true)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_id", nullable = false)
    private Workout workout;

    protected Exercise() {}  // JPA needs a no-arg constructor

    public Exercise(String name, Integer repRangeLower, Integer repRangeUpper, Integer sets, Integer restSeconds, String note, Workout workout) {
        this.name = name;
        this.repRangeLower = repRangeLower;
        this.repRangeUpper = repRangeUpper;
        this.sets = sets;
        this.restSeconds = restSeconds;
        this.note = note;
        this.workout = workout;
    }

    // TODO: Consider adding a one-to-many relationship to a SetHistory or ExerciseSet entity
    // to track individual set performance over time (reps, weight, etc.)
}
