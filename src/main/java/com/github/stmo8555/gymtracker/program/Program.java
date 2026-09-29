package com.github.stmo8555.gymtracker.program;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.github.stmo8555.gymtracker.user.User;

@Entity
@Table(name = "programs")
@Setter
@Getter
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean active = false;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 1")
    private Integer day = 1;

    @Column(columnDefinition = "INT DEFAULT 1")
    private Integer rotationPos = 1;

    // null = no rotation limit, the program repeats indefinitely
    @Column(nullable = true)
    private Integer rotations;

    @Column(nullable = true)
    private Integer deloadInterval;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    protected Program() {}

    public Program(String name, User user) {
        this.name = name;
        this.user = user;
    }
}
