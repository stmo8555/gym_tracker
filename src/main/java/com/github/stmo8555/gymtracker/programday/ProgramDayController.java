package com.github.stmo8555.gymtracker.programday;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.github.stmo8555.gymtracker.program.Program;
import com.github.stmo8555.gymtracker.program.ProgramRepository;
import com.github.stmo8555.gymtracker.workout.Workout;
import com.github.stmo8555.gymtracker.workout.WorkoutRepository;

@Controller
@RequestMapping("/program-days")
public class ProgramDayController {

    private final ProgramDayRepository repo;
    private final ProgramRepository programRepo;
    private final WorkoutRepository workoutRepo;

    public ProgramDayController(ProgramDayRepository repo, ProgramRepository programRepo, WorkoutRepository workoutRepo) {
        this.repo = repo;
        this.programRepo = programRepo;
        this.workoutRepo = workoutRepo;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("programDays", repo.findAll());
        model.addAttribute("programs", programRepo.findAll());
        model.addAttribute("workouts", workoutRepo.findAll());
        return "program-days";
    }

    @PostMapping
    public String create(@RequestParam Integer programId, @RequestParam Integer dayOrder,
            @RequestParam(required = false) String workoutId) {
        Program program = programRepo.getReferenceById(programId);
        Workout workout = parseId(workoutId) != null ? workoutRepo.getReferenceById(parseId(workoutId)) : null;
        repo.save(new ProgramDay(program, dayOrder, workout));
        return "redirect:/program-days";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("programDay", repo.findById(id).orElseThrow());
        model.addAttribute("programs", programRepo.findAll());
        model.addAttribute("workouts", workoutRepo.findAll());
        return "program-day-edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @RequestParam Integer programId, @RequestParam Integer dayOrder,
            @RequestParam(required = false) String workoutId) {
        ProgramDay programDay = repo.findById(id).orElseThrow();
        programDay.setProgram(programRepo.getReferenceById(programId));
        programDay.setDayOrder(dayOrder);
        programDay.setWorkout(parseId(workoutId) != null ? workoutRepo.getReferenceById(parseId(workoutId)) : null);
        repo.save(programDay);
        return "redirect:/program-days";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id) {
        repo.deleteById(id);
        return "redirect:/program-days";
    }

    private static Integer parseId(String value) {
        return (value == null || value.isBlank()) ? null : Integer.valueOf(value);
    }
}
