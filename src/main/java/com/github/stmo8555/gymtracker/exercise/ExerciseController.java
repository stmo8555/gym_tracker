package com.github.stmo8555.gymtracker.exercise;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.github.stmo8555.gymtracker.workout.Workout;
import com.github.stmo8555.gymtracker.workout.WorkoutRepository;

@Controller
@RequestMapping("/exercises")
public class ExerciseController {

    private final ExerciseRepository repo;
    private final WorkoutRepository workoutRepo;

    public ExerciseController(ExerciseRepository repo, WorkoutRepository workoutRepo) {
        this.repo = repo;
        this.workoutRepo = workoutRepo;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("exercises", repo.findAll());
        model.addAttribute("workouts", workoutRepo.findAll());
        return "exercises";
    }

    @PostMapping
    public String create(@RequestParam String name, @RequestParam Integer repRangeLower,
            @RequestParam Integer repRangeUpper, @RequestParam Integer sets,
            @RequestParam(defaultValue = "0") Integer restSeconds, @RequestParam(required = false) String note,
            @RequestParam Integer workoutId) {
        Workout workout = workoutRepo.getReferenceById(workoutId);
        repo.save(new Exercise(name, repRangeLower, repRangeUpper, sets, restSeconds, note, workout));
        return "redirect:/exercises";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("exercise", repo.findById(id).orElseThrow());
        model.addAttribute("workouts", workoutRepo.findAll());
        return "exercise-edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @RequestParam String name, @RequestParam Integer repRangeLower,
            @RequestParam Integer repRangeUpper, @RequestParam Integer sets,
            @RequestParam(defaultValue = "0") Integer restSeconds, @RequestParam(required = false) String note,
            @RequestParam Integer workoutId) {
        Exercise exercise = repo.findById(id).orElseThrow();
        exercise.setName(name);
        exercise.setRepRangeLower(repRangeLower);
        exercise.setRepRangeUpper(repRangeUpper);
        exercise.setSets(sets);
        exercise.setRestSeconds(restSeconds);
        exercise.setNote(note);
        exercise.setWorkout(workoutRepo.getReferenceById(workoutId));
        repo.save(exercise);
        return "redirect:/exercises";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id) {
        repo.deleteById(id);
        return "redirect:/exercises";
    }
}
