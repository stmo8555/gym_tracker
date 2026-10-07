package com.github.stmo8555.gymtracker.workout;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.github.stmo8555.gymtracker.exercise.*;
import com.github.stmo8555.gymtracker.programday.ProgramDay;
import com.github.stmo8555.gymtracker.programday.ProgramDayRepository;
import com.github.stmo8555.gymtracker.user.MockUserProvider;

@Controller
@RequestMapping("/workouts")
public class WorkoutController {

    private final WorkoutRepository repo;
    private final ExerciseRepository exerciseRepo;
    private final ProgramDayRepository programDayRepo;
    private final MockUserProvider mockUser;

    public WorkoutController(WorkoutRepository repo, ExerciseRepository exerciseRepo,
            ProgramDayRepository programDayRepo, MockUserProvider mockUser) {
        this.repo = repo;
        this.exerciseRepo = exerciseRepo;
        this.programDayRepo = programDayRepo;
        this.mockUser = mockUser;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("workouts", repo.findAll());
        return "workouts";
    }

    @PostMapping
    public String create(@RequestParam() String name) {
        repo.save(new Workout(name, mockUser.get()));
        return "redirect:/workouts";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("workout", repo.findById(id).orElseThrow());
        model.addAttribute("exercises", exerciseRepo.findByWorkoutIdOrderByPositionAsc(id));
        // programs using this workout, for the "used in" links. distinct() is safe
        // on entities here: within one persistence context each program id maps
        // to a single instance.
        model.addAttribute("programs", programDayRepo.findByWorkoutId(id).stream()
                .map(ProgramDay::getProgram)
                .distinct()
                .toList());
        return "workout-edit";
    }

    @PostMapping("/{id}/exercises")
    public String addExercise(@PathVariable Integer id, @RequestParam String name, @RequestParam Integer repRangeLower,
            @RequestParam Integer repRangeUpper, @RequestParam Integer sets,
            @RequestParam(defaultValue = "0") Integer restSeconds, @RequestParam(required = false) String note) {
        Workout workout = repo.getReferenceById(id);
        int position = exerciseRepo.findFirstByWorkoutIdOrderByPositionDesc(id)
                .map(last -> last.getPosition() + 1)
                .orElse(0);
        exerciseRepo.save(new Exercise(name, repRangeLower, repRangeUpper, sets, restSeconds, note, position, workout));
        return "redirect:/workouts/" + id + "/edit";
    }

    @GetMapping("/{wid}/exercises/{id}/edit")
    public String editExcersice(@PathVariable Integer wid, @PathVariable Integer id, Model model) {
        model.addAttribute("workout", repo.findById(wid).orElseThrow());
        model.addAttribute("exercise", exerciseRepo.findById(id).orElseThrow());
        return "exercise-edit";
    }

    @PostMapping("/{wid}/exercises/{id}/delete")
    public String delete(@PathVariable Integer wid, @PathVariable Integer id) {
        exerciseRepo.deleteById(id);
        return "redirect:/workouts/" + wid + "/edit";
    }

    @PostMapping("/{wid}/exercises/{id}")
    public String updateExcersise(@PathVariable Integer wid, @PathVariable Integer id, @RequestParam String name,
            @RequestParam Integer repRangeLower,
            @RequestParam Integer repRangeUpper, @RequestParam Integer sets,
            @RequestParam(defaultValue = "0") Integer restSeconds, @RequestParam(required = false) String note) {
        Exercise exercise = exerciseRepo.findById(id).orElseThrow();
        exercise.setName(name);
        exercise.setRepRangeLower(repRangeLower);
        exercise.setRepRangeUpper(repRangeUpper);
        exercise.setSets(sets);
        exercise.setRestSeconds(restSeconds);
        exercise.setNote(note);
        exercise.setWorkout(repo.getReferenceById(wid));
        exerciseRepo.save(exercise);
        return "redirect:/workouts/" + wid + "/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Integer id, @RequestParam String name) {
        Workout workout = repo.findById(id).orElseThrow();
        workout.setName(name);
        repo.save(workout);
        return "redirect:/workouts";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id) {
        // program_days.workout_id is ON DELETE RESTRICT; fail with a clear status
        // instead of a constraint violation
        if (!programDayRepo.findByWorkoutId(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Workout is used in a program; remove it from the program first");
        }
        repo.deleteById(id);
        return "redirect:/workouts";
    }
}
