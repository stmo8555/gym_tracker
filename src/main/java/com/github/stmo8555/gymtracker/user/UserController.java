package com.github.stmo8555.gymtracker.user;

import org.springframework.stereotype.Controller;

@Controller
public class UserController {

    private final UserRepository repo;

    public UserController(UserRepository repo) { this.repo = repo; }
}
