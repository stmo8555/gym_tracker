package com.github.stmo8555.gymtracker.home;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.github.stmo8555.gymtracker.user.MockUserProvider;
import com.github.stmo8555.gymtracker.program.ProgramRepository;
import com.github.stmo8555.gymtracker.programday.*;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping("/")
public class HomeController {

    private final ProgramRepository programRepository;
    private final ProgramDayRepository programDayRepository;
    private final MockUserProvider mockUserProvider;

    public HomeController(ProgramDayRepository programDayRepository, MockUserProvider mockUserProvider,
            ProgramRepository programRepository) {
        this.programRepository = programRepository;
        this.programDayRepository = programDayRepository;
        this.mockUserProvider = mockUserProvider;
    }

    // todo make this more idiomatic
    @GetMapping
    public String list(Model model) {
        var activeProgram = programRepository.findByUserIdAndActiveTrue(mockUserProvider.get().getId());
        var programDay = activeProgram
                .flatMap(program -> programDayRepository.findByProgramIdAndDayOrder(program.getId(), program.getDay()));
        var workout = programDay.map(pd -> pd.getWorkout()).orElse(null);
        model.addAttribute("workout", workout);
        model.addAttribute("programDay", programDay.orElse(null));
        return "home";
    }

}
