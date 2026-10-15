package com.github.stmo8555.gymtracker.program;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.stmo8555.gymtracker.programday.ProgramDayRepository;

@Service
public class ProgramService {

    private final ProgramDayRepository programDayRepository;

    public ProgramService(ProgramDayRepository programDayRepository) {
        this.programDayRepository = programDayRepository;
    }

    // moves to the next program day, wrapping back to the first after the last
    @Transactional
    public void advanceDay(Program program) {
        var days = programDayRepository.countByProgramId(program.getId());
        program.setDay((program.getDay() + 1) % days);
        if ((program.getDay() + 1) % days > 0) {
            program.setRotationPos(program.getRotationPos() + 1);  
        }
    }
}
