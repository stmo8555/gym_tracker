package com.github.stmo8555.gymtracker.workout;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.github.stmo8555.gymtracker.program.Program;
import com.github.stmo8555.gymtracker.program.ProgramRepository;
import com.github.stmo8555.gymtracker.user.MockUserProvider;

@Controller
@RequestMapping("/workouts")
public class WorkoutController {

    private final WorkoutRepository repo;
    private final ProgramRepository programRepo;
    private final MockUserProvider mockUser;

    public WorkoutController(WorkoutRepository repo, ProgramRepository programRepo, MockUserProvider mockUser) {
        this.repo = repo;
        this.programRepo = programRepo;
        this.mockUser = mockUser;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("workouts", repo.findAll());
        model.addAttribute("programs", programRepo.findAll());
        return "workouts";
    }

    @PostMapping
    public String create(@RequestParam(required = false) String programId) {
        Program program = parseId(programId) != null ? programRepo.getReferenceById(parseId(programId)) : null;
        repo.save(new Workout(mockUser.get(), program));
        return "redirect:/workouts";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("workout", repo.findById(id).orElseThrow());
        model.addAttribute("programs", programRepo.findAll());
        return "workout-edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @RequestParam(required = false) String programId) {
        Workout workout = repo.findById(id).orElseThrow();
        workout.setProgram(parseId(programId) != null ? programRepo.getReferenceById(parseId(programId)) : null);
        repo.save(workout);
        return "redirect:/workouts";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id) {
        repo.deleteById(id);
        return "redirect:/workouts";
    }

    private static Integer parseId(String value) {
        return (value == null || value.isBlank()) ? null : Integer.valueOf(value);
    }
}
