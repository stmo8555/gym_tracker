package com.github.stmo8555.gymtracker.programday;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramDayRepository extends JpaRepository<ProgramDay, Integer> {
    // TODO: Disable open-in-view and use entity graphs / fetch joins for lazy associations used in templates
}
