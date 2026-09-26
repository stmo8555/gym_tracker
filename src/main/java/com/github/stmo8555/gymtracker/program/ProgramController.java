package com.github.stmo8555.gymtracker.program;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.github.stmo8555.gymtracker.workout.*;
import com.github.stmo8555.gymtracker.user.MockUserProvider;
import com.github.stmo8555.gymtracker.programday.*;

@Controller
@RequestMapping("/programs")
public class ProgramController {

    private final WorkoutRepository workoutRepository;
    private final ProgramRepository repo;
    private final ProgramDayRepository programDayRepository;
    private final MockUserProvider mockUser;

    public ProgramController(ProgramRepository repo, MockUserProvider mockUser,
            ProgramDayRepository programDayRepository, WorkoutRepository workoutRepository) {
        this.repo = repo;
        this.programDayRepository = programDayRepository;
        this.mockUser = mockUser;
        this.workoutRepository = workoutRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("programs", repo.findAll());
        return "programs";
    }

    @PostMapping
    public String create(@RequestParam String name) {
        repo.save(new Program(name, mockUser.get()));
        return "redirect:/programs";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("program", repo.findById(id).orElseThrow());
        model.addAttribute("programDays", programDayRepository.findByProgramId(id));
        model.addAttribute("workouts", workoutRepository.findAll());
        return "program-edit";
    }

    @PostMapping("/{id}/program-days")
    public String createProgramDays(@PathVariable Integer id, @RequestParam Integer dayOrder,
            @RequestParam(required = false) String workoutId) {
        // getReferenceById gives a lazy proxy for the FK without a real SELECT -
        // fine here since we only need it to set the relation, not read its fields.
        Program program = repo.getReferenceById(id);
        Workout workout = parseId(workoutId) != null ? workoutRepository.getReferenceById(parseId(workoutId)) : null;
        programDayRepository.save(new ProgramDay(program, dayOrder, workout));
        // redirect (not just returning a view) so refreshing the result page
        // doesn't resubmit the form - the Post/Redirect/Get pattern.
        return "redirect:/programs/" + id + "/edit";
    }

    @GetMapping("/{pid}/program-days/{id}/edit")
    public String editProgramDay(@PathVariable Integer pid, @PathVariable Integer id, Model model) {
        model.addAttribute("program", repo.findById(pid).orElseThrow());
        model.addAttribute("programDay", programDayRepository.findById(id).orElseThrow());
        model.addAttribute("workouts", workoutRepository.findAll());
        return "program-day-edit";
    }

    @PostMapping("/{pid}/program-days/{id}")
    public String updateProgramDay(@PathVariable Integer pid, @PathVariable Integer id, @RequestParam Integer dayOrder,
            @RequestParam(required = false) String workoutId) {
        ProgramDay programDay = programDayRepository.findById(id).orElseThrow();
        programDay.setDayOrder(dayOrder);
        programDay.setWorkout(parseId(workoutId) != null ? workoutRepository.getReferenceById(parseId(workoutId)) : null);
        programDayRepository.save(programDay);
        return "redirect:/programs/" + pid + "/edit";
    }

    @PostMapping("/{pid}/program-days/{id}/delete")
    public String deleteProgramDay(@PathVariable Integer pid, @PathVariable Integer id) {
        programDayRepository.deleteById(id);
        return "redirect:/programs/" + pid + "/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @RequestParam String name,
            @RequestParam(defaultValue = "false") boolean active, @RequestParam Integer day,
            @RequestParam Integer rotationPos, @RequestParam Integer rotations,
            @RequestParam(required = false) String deloadInterval) {
        Program program = repo.findById(id).orElseThrow();
        program.setName(name);
        program.setActive(active);
        program.setDay(day);
        program.setRotationPos(rotationPos);
        program.setRotations(rotations);
        program.setDeloadInterval(
                (deloadInterval == null || deloadInterval.isBlank()) ? null : Integer.valueOf(deloadInterval));
        repo.save(program);
        return "redirect:/programs";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id) {
        repo.deleteById(id);
        return "redirect:/programs";
    }

    private static Integer parseId(String value) {
        return (value == null || value.isBlank()) ? null : Integer.valueOf(value);
    }
}
