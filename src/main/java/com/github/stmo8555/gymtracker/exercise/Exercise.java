package com.github.stmo8555.gymtracker.exercise;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.github.stmo8555.gymtracker.workout.Workout;

@Entity
@Table(name = "exercises", uniqueConstraints = @UniqueConstraint(columnNames = {"workout_id", "position"}))
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

    // 0-based order within the workout; gaps are fine, only the ordering matters
    @Column(nullable = false)
    private Integer position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_id", nullable = false)
    private Workout workout;

    protected Exercise() {}  // JPA needs a no-arg constructor

    public Exercise(String name, Integer repRangeLower, Integer repRangeUpper, Integer sets, Integer restSeconds, String note, Integer position, Workout workout) {
        this.name = name;
        this.repRangeLower = repRangeLower;
        this.repRangeUpper = repRangeUpper;
        this.sets = sets;
        this.restSeconds = restSeconds;
        this.note = note;
        this.position = position;
        this.workout = workout;
    }

    // "8" for a fixed target, "8–12" for a range. Not a column: Hibernate maps
    // fields here (@Id is on a field), so getters like this are ignored.
    public String getRepRangeLabel() {
        return repRangeLower.equals(repRangeUpper) ? repRangeLower.toString() : repRangeLower + "–" + repRangeUpper;
    }

    // TODO: Consider adding a one-to-many relationship to a SetHistory or ExerciseSet entity
    // to track individual set performance over time (reps, weight, etc.)
}
