package com.github.stmo8555.gymtracker.programday;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;

// TODO: Disable open-in-view and use entity graphs / fetch joins for lazy associations used in templates
public interface ProgramDayRepository extends JpaRepository<ProgramDay, Integer> {
	@Nullable
	List<ProgramDay> findByProgramId(Integer id);
}
