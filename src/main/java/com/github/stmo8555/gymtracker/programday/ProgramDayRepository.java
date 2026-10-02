package com.github.stmo8555.gymtracker.programday;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

// TODO: Disable open-in-view and use entity graphs / fetch joins for lazy associations used in templates
public interface ProgramDayRepository extends JpaRepository<ProgramDay, Integer> {
    List<ProgramDay> findByProgramIdOrderByDayOrderAsc(Integer id);

    List<ProgramDay> findByProgramId(Integer id);

    Optional<ProgramDay> findByProgramIdAndDayOrder(Integer pid, Integer day);

    Optional<ProgramDay> findByIdAndProgramId(Integer id, Integer programId);

    Integer countByProgramId(Integer programId);

    List<ProgramDay> findByWorkoutId(Integer workoutId);
}
