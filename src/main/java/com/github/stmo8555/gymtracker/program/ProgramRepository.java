package com.github.stmo8555.gymtracker.program;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramRepository extends JpaRepository<Program, Integer> {
    List<Program> findByUserId(Integer userId);

    Optional<Program> findByIdAndUserId(Integer id, Integer userId);

    Optional<Program> findByUserIdAndActiveTrue(Integer userId);
}
