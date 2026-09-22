package com.github.stmo8555.gymtracker.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Setter
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false)
    private String pwd;

    protected User() {
    } // JPA needs a no-arg constructor

    public User(String text, String pwd) {
        this.username = text;
        this.pwd = pwd;
    }
}
