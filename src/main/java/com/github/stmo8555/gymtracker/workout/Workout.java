package com.github.stmo8555.gymtracker.workout;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.github.stmo8555.gymtracker.user.User;
import com.github.stmo8555.gymtracker.program.Program;

@Entity
@Table(name = "workouts")
@Getter
@Setter
public class Workout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = true)
    private Program program;

    protected Workout() {}                 // JPA needs a no-arg constructor

    public Workout(User user, Program program) {
        this.user = user;
        this.program = program;
    }

    public Workout(User user) {
        this.user = user;
        this.program = null;
    }
}
