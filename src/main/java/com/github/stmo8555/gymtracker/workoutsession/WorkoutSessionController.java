package com.github.stmo8555.gymtracker.workoutsession;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.github.stmo8555.gymtracker.exercise.*;
import com.github.stmo8555.gymtracker.workout.*;
import com.github.stmo8555.gymtracker.program.ProgramRepository;
import com.github.stmo8555.gymtracker.programday.ProgramDayRepository;
import com.github.stmo8555.gymtracker.user.MockUserProvider;

@Controller
@RequestMapping("/workoutSession")
public class WorkoutSessionController {

    private final WorkoutSessionSetRepository workoutSessionSetRepository;
    private final MockUserProvider mockUserProvider;
    private final WorkoutSessionRepository repo;
    private final WorkoutRepository workoutRepo;
    private final ExerciseRepository exerciseRepo;
    private final ProgramRepository programRepo;
    private final ProgramDayRepository programDayRepo;

    public WorkoutSessionController(WorkoutRepository workoutRepo, ExerciseRepository exerciseRepo,
            ProgramRepository programRepo, ProgramDayRepository programDayRepo, WorkoutSessionRepository repo,
            MockUserProvider mockUserProvider, WorkoutSessionSetRepository workoutSessionSetRepository) {
        this.repo = repo;
        this.workoutRepo = workoutRepo;
        this.exerciseRepo = exerciseRepo;
        this.programRepo = programRepo;
        this.programDayRepo = programDayRepo;
        this.mockUserProvider = mockUserProvider;
        this.workoutSessionSetRepository = workoutSessionSetRepository;
    }

    @GetMapping
    // the in-progress session; finished sessions are hidden (plan: out of scope for v1)
    public String list(Model model) {
        var session = repo.findByUserIdAndFinishedAtIsNull(mockUserProvider.get().getId());
        if (session.isEmpty()) {
            return "redirect:/";
        }
        // not "session": Thymeleaf reserves that name for the HTTP session
        model.addAttribute("workoutSession", session.get());

        var setsByExercise = workoutSessionSetRepository
                .findByWorkoutSessionIdOrderByIdAsc(session.get().getId())
                .stream()
                // LinkedHashMap keeps the workout's exercise order (sets are loaded in that order)
                .collect(Collectors.groupingBy(WorkoutSessionSet::getExerciseName, LinkedHashMap::new,
                        Collectors.toList()));

        model.addAttribute("setsByExercise", setsByExercise);
        return "workout-session";
    }

    // starts the active program's current day; the server picks the day so the
    // client can't start an arbitrary (or someone else's) program day
    @Transactional
    @PostMapping
    public String start() {
        var user = mockUserProvider.get();
        if (repo.findByUserIdAndFinishedAtIsNull(user.getId()).isPresent()) {
            return "redirect:/workoutSession";
        }
        var program = programRepo.findByUserIdAndActiveTrue(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No active program"));
        var programDay = programDayRepo.findByProgramIdAndDayOrder(program.getId(), program.getDay())
                .orElseThrow();
        var workout = programDay.getWorkout();
        if (workout == null) {
            // rest days auto-advance in step 6; until then just refuse
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Today is a rest day");
        }
        var session = repo.save(new WorkoutSession(user, program, workout, programDay.getDayOrder(), Instant.now()));
        for (Exercise exercise : exerciseRepo.findByWorkoutIdOrderByPositionAsc(workout.getId())) {
            for (int i = 0; i < exercise.getSets(); i++) {
                // reps/weight empty until prefill (step 4)
                workoutSessionSetRepository.save(
                        new WorkoutSessionSet(session, exercise, exercise.getName(), i, null, null, null));
            }
        }
        return "redirect:/workoutSession";
        }

    //Todo check ownership with session Id
    @PostMapping("/{sessionId}/set/{id}")
    public String setCompleted(@PathVariable Integer sessionId, @PathVariable Integer id, @RequestParam(defaultValue = "false") boolean finished) {
        var workoutSessionSet = workoutSessionSetRepository.findById(id).orElseThrow();  

        workoutSessionSet.setFinishedAt( finished ? Instant.now() : null);
        workoutSessionSetRepository.save(workoutSessionSet); 
        return "redirect:/workoutSession";
    }

    @PostMapping("/{id}/exercises")
    public String addExercise(@PathVariable Integer id, @RequestParam String name, @RequestParam Integer repRangeLower,
            @RequestParam Integer repRangeUpper, @RequestParam Integer sets,
            @RequestParam(defaultValue = "0") Integer restSeconds, @RequestParam(required = false) String note) {
        Workout workout = workoutRepo.getReferenceById(id);
        int position = exerciseRepo.findFirstByWorkoutIdOrderByPositionDesc(id)
                .map(last -> last.getPosition() + 1)
                .orElse(0);
        exerciseRepo.save(new Exercise(name, repRangeLower, repRangeUpper, sets, restSeconds, note, position, workout));
        return "redirect:/workouts/" + id + "/edit";
    }

    @GetMapping("/{wid}/exercises/{id}/edit")
    public String editExcersice(@PathVariable Integer wid, @PathVariable Integer id, Model model) {
        model.addAttribute("workout", workoutRepo.findById(wid).orElseThrow());
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
        exercise.setWorkout(workoutRepo.getReferenceById(wid));
        exerciseRepo.save(exercise);
        return "redirect:/workouts/" + wid + "/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Integer id, @RequestParam String name) {
        Workout workout = workoutRepo.findById(id).orElseThrow();
        workout.setName(name);
        workoutRepo.save(workout);
        return "redirect:/workouts";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id) {
        workoutRepo.deleteById(id);
        return "redirect:/workouts";
    }
}
