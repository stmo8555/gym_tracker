package com.github.stmo8555.gymtracker.home;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.github.stmo8555.gymtracker.workout.*;
import com.github.stmo8555.gymtracker.user.MockUserProvider;
import com.github.stmo8555.gymtracker.program.ProgramRepository;
import com.github.stmo8555.gymtracker.programday.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
            var workout = activeProgram
                .map(program -> {
                    var day = program.getDay();
                    var id = program.getId();
                    var programDay = programDayRepository.findByProgramIdAndDayOrder(id, day);
                    return programDay.orElseThrow().getWorkout();
                })
                .orElse(null);

            model.addAttribute("workout", workout);
            return "home";
        }

}
