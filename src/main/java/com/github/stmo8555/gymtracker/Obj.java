package com.github.stmo8555.gymtracker;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
public class Obj {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Setter
    @Column(nullable = false, unique = true)
    private String text;

    protected Obj() {}                 // JPA needs a no-arg constructor

    public Obj(String text) { this.text = text; }
}
