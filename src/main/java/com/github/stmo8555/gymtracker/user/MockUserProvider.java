package com.github.stmo8555.gymtracker.user;

import org.springframework.stereotype.Component;

// Stand-in for "the current user" until auth exists. Single-user mode: everything
// hangs off one lazily-created row so Program/Workout don't need a user picker yet.
@Component
public class MockUserProvider {

    private final UserRepository userRepo;

    public MockUserProvider(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public User get() {
        return userRepo.findAll().stream().findFirst()
                .orElseGet(() -> userRepo.save(new User("mock", "mock")));
    }
}
