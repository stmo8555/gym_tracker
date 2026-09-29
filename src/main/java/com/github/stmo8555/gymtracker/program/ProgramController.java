package com.github.stmo8555.gymtracker.program;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
        model.addAttribute("programs", repo.findByUserId(mockUser.get().getId()));
        return "programs";
    }

    @PostMapping
    public String create(@RequestParam String name) {
        repo.save(new Program(name, mockUser.get()));
        return "redirect:/programs";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("program", findOwnedProgram(id));
        model.addAttribute("programDays", programDayRepository.findByProgramId(id));
        model.addAttribute("workouts", workoutRepository.findAll());
        return "program-edit";
    }

    @PostMapping("/{id}/program-days")
    public String createProgramDays(@PathVariable Integer id, @RequestParam(required = false) Integer workoutId) {
        var dayCount = programDayRepository.countByProgramId(id);
        Program program = findOwnedProgram(id);
        Workout workout = workoutId != null ? workoutRepository.getReferenceById(workoutId) : null;
        programDayRepository.save(new ProgramDay(program, dayCount, workout));
        return "redirect:/programs/" + id + "/edit";
    }

    @GetMapping("/{pid}/program-days/{id}/edit")
    public String editProgramDay(@PathVariable Integer pid, @PathVariable Integer id, Model model) {
        model.addAttribute("program", findOwnedProgram(pid));
        model.addAttribute("programDay", findProgramDay(pid, id));
        model.addAttribute("workouts", workoutRepository.findAll());
        return "program-day-edit";
    }

    @PostMapping("/{pid}/program-days/{id}")
    public String updateProgramDay(@PathVariable Integer pid, @PathVariable Integer id,
            @RequestParam(required = false) Integer workoutId) {
        findOwnedProgram(pid);
        ProgramDay programDay = findProgramDay(pid, id);
        programDay.setWorkout(workoutId != null ? workoutRepository.getReferenceById(workoutId) : null);
        programDayRepository.save(programDay);
        return "redirect:/programs/" + pid + "/edit";
    }

    @PostMapping("/{pid}/program-days/{id}/delete")
    public String deleteProgramDay(@PathVariable Integer pid, @PathVariable Integer id) {
        findOwnedProgram(pid);
        programDayRepository.delete(findProgramDay(pid, id));
        programDayRepository.flush();

        var programDays = programDayRepository.findByProgramIdOrderByDayOrderAsc(mockUser.get().getId());
        for (int i = 0; i < programDays.size(); i++) {
            programDays.get(i).setDayOrder(i);
        }
          
        programDayRepository.saveAll(programDays);
        return "redirect:/programs/" + pid + "/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @RequestParam String name,
            @RequestParam Integer day, @RequestParam Integer rotationPos,
            @RequestParam(required = false) Integer rotations, @RequestParam(required = false) Integer deloadInterval) {
        Program program = findOwnedProgram(id);
        program.setName(name);
        program.setDay(day);
        program.setRotationPos(rotationPos);
        program.setRotations(rotations);
        program.setDeloadInterval(deloadInterval);
        repo.save(program);
        return "redirect:/programs";
    }

    @Transactional
    @PostMapping("/{id}/activate")
    public String activate(@PathVariable Integer id) {
        Program program = findOwnedProgram(id);

        repo.findByUserIdAndActiveTrue(mockUser.get().getId()).ifPresent(current -> {
            current.setActive(false);
            repo.flush();
        });

        program.setActive(true);
        return "redirect:/programs";
    }

    @Transactional
    @PostMapping("/{id}/program-days/workout")
    public String addProgramAndCreateWorkout(@PathVariable Integer id,
            @RequestParam String workoutName) {
        var program = findOwnedProgram(id);
        var dayCount = programDayRepository.countByProgramId(id);

        var workout = workoutRepository.save(new Workout(workoutName, mockUser.get()));
        programDayRepository.save(new ProgramDay(program, dayCount + 1, workout));

        return "redirect:/workouts/" + workout.getId() + "/edit";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id) {
        repo.delete(findOwnedProgram(id));
        return "redirect:/programs";
    }

    // 404 (not 403) for other users' programs, so ids of programs you don't own
    // aren't distinguishable from ones that don't exist.
    private Program findOwnedProgram(Integer id) {
        return repo.findByIdAndUserId(id, mockUser.get().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    // checks the day actually belongs to the program in the URL; call
    // findOwnedProgram(pid) first so ownership of the program is checked too.
    private ProgramDay findProgramDay(Integer pid, Integer id) {
        return programDayRepository.findByIdAndProgramId(id, pid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
