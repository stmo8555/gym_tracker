package com.github.stmo8555.gymtracker.programday;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.github.stmo8555.gymtracker.program.Program;
import com.github.stmo8555.gymtracker.workout.Workout;

@Entity
@Table(name = "program_days", uniqueConstraints = @UniqueConstraint(columnNames = {"program_id", "day_order"}))
@Getter
@Setter
public class ProgramDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @Column(nullable = false)
    private Integer dayOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_id", nullable = true)
    private Workout workout;

    protected ProgramDay() {}  // JPA needs a no-arg constructor

    public ProgramDay(Program program, Integer dayOrder, Workout workout) {
        this.program = program;
        this.dayOrder = dayOrder;
        this.workout = workout;
    }

    public ProgramDay(Program program, Integer dayOrder) {
        this.program = program;
        this.dayOrder = dayOrder;
        this.workout = null;
    }
}
