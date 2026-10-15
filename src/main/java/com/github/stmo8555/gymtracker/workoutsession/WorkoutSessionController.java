package com.github.stmo8555.gymtracker.workoutsession;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
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
import com.github.stmo8555.gymtracker.program.ProgramService;
import com.github.stmo8555.gymtracker.programday.ProgramDayRepository;
import com.github.stmo8555.gymtracker.user.MockUserProvider;

@Controller
@RequestMapping("/workoutSession")
public class WorkoutSessionController {

    private final WorkoutSessionSetRepository workoutSessionSetRepository;
    private final MockUserProvider mockUserProvider;
    private final WorkoutSessionRepository repo;
    private final ExerciseRepository exerciseRepo;
    private final ProgramRepository programRepo;
    private final ProgramDayRepository programDayRepo;
    private final ProgramService programService;

    public WorkoutSessionController(ExerciseRepository exerciseRepo,
            ProgramRepository programRepo, ProgramDayRepository programDayRepo, WorkoutSessionRepository repo,
            MockUserProvider mockUserProvider, WorkoutSessionSetRepository workoutSessionSetRepository,
            ProgramService programService) {
        this.repo = repo;
        this.programService = programService;
        this.exerciseRepo = exerciseRepo;
        this.programRepo = programRepo;
        this.programDayRepo = programDayRepo;
        this.mockUserProvider = mockUserProvider;
        this.workoutSessionSetRepository = workoutSessionSetRepository;
    }

    @GetMapping
    // the in-progress session; finished sessions are hidden (plan: out of scope for
    // v1)
    public String list(Model model) {
        var userId = mockUserProvider.get().getId();
        var session = repo.findByUserIdAndFinishedAtIsNull(userId).orElse(null);
        if (session == null) {
            return "redirect:/";
        }
        // not "session": Thymeleaf reserves that name for the HTTP session
        model.addAttribute("workoutSession", session);

        // LinkedHashMap keeps the workout's exercise order (sets are loaded in that
        // order)
        model.addAttribute("setsByExercise", workoutSessionSetRepository
                .findByWorkoutSessionIdOrderByIdAsc(session.getId())
                .stream()
                .collect(Collectors.groupingBy(WorkoutSessionSet::getExerciseName, LinkedHashMap::new,
                        Collectors.toList())));
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
        var prevSets = previousSets(user.getId(), workout.getId());

        var session = repo.save(new WorkoutSession(user, program, workout, programDay.getDayOrder(), Instant.now()));
        for (Exercise exercise : exerciseRepo.findByWorkoutIdOrderByPositionAsc(workout.getId())) {
            for (int i = 0; i < exercise.getSets(); i++) {
                var prev = prevSets.get(setKey(exercise.getName(), i));
                workoutSessionSetRepository.save(new WorkoutSessionSet(session, exercise, exercise.getName(), i,
                        prev != null ? prev.getReps() : null,
                        prev != null ? prev.getWeight() : null,
                        null));
            }
        }
        return "redirect:/workoutSession";
    }

    // sets of the last finished session of this workout, keyed by setKey, so a new
    // session can prefill reps/weight from them
    private Map<String, WorkoutSessionSet> previousSets(Integer userId, Integer workoutId) {
        var prev = repo.findLastFinished(userId, workoutId);
        if (prev.isEmpty()) {
            return Map.of();
        }
        var sets = workoutSessionSetRepository.findByWorkoutSessionIdOrderByIdAsc(prev.get().getId());
        // keep the first on duplicates (same exercise name twice in a workout)
        return sets.stream().collect(Collectors.toMap(
                set -> setKey(set.getExerciseName(), set.getSetNumber()), set -> set, (a, b) -> a));
    }

    private static String setKey(String exerciseName, int setNumber) {
        return exerciseName + "#" + setNumber;
    }

    @PostMapping("/{sessionId}/set/{id}")
    public String setCompleted(@PathVariable Integer sessionId, @PathVariable Integer id,
            @RequestParam(defaultValue = "false") boolean finished,
            @RequestParam Integer reps,
            @RequestParam BigDecimal weight) {

        var workoutSessionSet = workoutSessionSetRepository.findById(id).orElseThrow();
        workoutSessionSet.setReps(reps);
        workoutSessionSet.setWeight(weight);

        workoutSessionSet.setFinishedAt(finished ? Instant.now() : null);
        workoutSessionSetRepository.save(workoutSessionSet);

        return "redirect:/workoutSession";
    }

    @PostMapping("/{id}/stop")
    public String stop(@PathVariable Integer id) {
        repo.deleteById(id);
        return "redirect:/";
    }

    @Transactional
    @PostMapping("/{id}/finish")
    public String update(@PathVariable Integer id) {
        var workoutSession = repo.findByUserIdAndFinishedAtIsNull(mockUserProvider.get().getId()).orElseThrow();
        workoutSession.setFinishedAt(Instant.now());

        var program = programRepo.findByIdAndUserId(workoutSession.getProgram().getId(), mockUserProvider.get().getId())
                .orElseThrow();
        programService.advanceDay(program);

        return "redirect:/";
    }
}
