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
    // ProgramDay has FKs to both Program and Workout, and the forms need dropdowns
    // for them, so those repos are injected here too.
    private final ProgramRepository programRepo;
    private final WorkoutRepository workoutRepo;

    public ProgramDayController(ProgramDayRepository repo, ProgramRepository programRepo,
            WorkoutRepository workoutRepo) {
        this.repo = repo;
        this.programRepo = programRepo;
        this.workoutRepo = workoutRepo;
    }

    // GET /program-days
    @GetMapping
    public String list(Model model) {
        model.addAttribute("programDays", repo.findAll());
        // programs/workouts are loaded too, just to populate the <select> dropdowns
        // in the create form on this same page.
        model.addAttribute("programs", programRepo.findAll());
        model.addAttribute("workouts", workoutRepo.findAll());
        return "program-days"; // renders templates/program-days.html
    }

    // POST /program-days, hit by the "add" form on the list page
    @PostMapping
    public String create(@RequestParam Integer programId, @RequestParam Integer dayOrder,
            @RequestParam(required = false) String workoutId) {
        // getReferenceById gives a lazy proxy for the FK without a real SELECT -
        // fine here since we only need it to set the relation, not read its fields.
        Program program = programRepo.getReferenceById(programId);
        Workout workout = parseId(workoutId) != null ? workoutRepo.getReferenceById(parseId(workoutId)) : null;
        repo.save(new ProgramDay(program, dayOrder, workout));
        // redirect (not just returning a view) so refreshing the result page
        // doesn't resubmit the form - the Post/Redirect/Get pattern.
        return "redirect:/program-days";
    }

    // GET /program-days/{id}/edit
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Integer id, Model model) {
        // orElseThrow: no custom 404 handling yet, an unknown id just blows up
        // into Spring's default error page.
        model.addAttribute("programDay", repo.findById(id).orElseThrow());
        model.addAttribute("programs", programRepo.findAll());
        model.addAttribute("workouts", workoutRepo.findAll());
        return "program-day-edit";
    }

    // POST /program-days/{id}, hit by the edit form
    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @RequestParam Integer programId, @RequestParam Integer dayOrder,
            @RequestParam(required = false) String workoutId) {
        ProgramDay programDay = repo.findById(id).orElseThrow();
        // mutate the already-persisted entity via its setters, then save() -
        // since it already has an id, JPA issues an UPDATE, not an INSERT.
        programDay.setProgram(programRepo.getReferenceById(programId));
        programDay.setDayOrder(dayOrder);
        programDay.setWorkout(parseId(workoutId) != null ? workoutRepo.getReferenceById(parseId(workoutId)) : null);
        repo.save(programDay);
        return "redirect:/program-days";
    }

    // POST /program-days/{id}/delete - plain HTML forms can't send a real DELETE
    // verb, so this is a POST to a /delete sub-path instead.
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id) {
        repo.deleteById(id);
        return "redirect:/program-days";
    }

    // workoutId comes in as a String, not Integer, because the form's
    // "-- no workout --" option submits an empty string, and Spring can't bind
    // "" to an Integer. This treats blank/missing as "no workout" (null).
    private static Integer parseId(String value) {
        return (value == null || value.isBlank()) ? null : Integer.valueOf(value);
    }
}
