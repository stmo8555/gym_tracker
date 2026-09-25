package com.github.stmo8555.gymtracker.workout;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.github.stmo8555.gymtracker.user.User;

import java.util.ArrayList;
import java.util.List;

import com.github.stmo8555.gymtracker.exercise.Exercise;
import com.github.stmo8555.gymtracker.program.Program;

@Entity
@Table(name = "workouts")
@Getter
@Setter
public class Workout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = true)
    private Program program;

    @OneToMany(mappedBy = "workout", fetch = FetchType.LAZY)
    private List<Exercise> exercises = new ArrayList<>();

    protected Workout() {
    } // JPA needs a no-arg constructor

    public Workout(String name, User user, Program program) {
        this.name = name;
        this.user = user;
        this.program = program;
    }

    public Workout(String name, User user) {
        this.name = name;
        this.user = user;
        this.program = null;
    }
}
