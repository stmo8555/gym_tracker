# Gym tracker

Spring Boot · Thymeleaf · Alpine AJAX · Postgres

## Build order

1. Foundations *(done)* Gradle project, Postgres container, Flyway with one init migration, a Thymeleaf page that renders and reaches the database.
2. Workouts *(done)* Create, edit, delete workouts and their exercises: name, target sets, rep range, rest seconds, notes. Plain CRUD.
3. Log a session *(in progress)* Start from a workout, add/edit/delete sets while in progress, press finish. Usable here, just not smart.
   - [x] Start from the active program's current day; set rows pre-created from the workout's target sets
   - [x] Session page lists sets grouped by exercise, in workout order
   - [ ] Log a set: one form per set row saving reps, weight and logged (tick); refuse edits to sets outside the user's in-progress session
   - [ ] Add / remove sets per exercise during the session
   - [ ] Optional "also save to workout" when changing the set count (updates the exercise's target sets; off by default)
   - [ ] Start any workout from the workouts list (no program; finishing it doesn't move any program position)
   - [ ] Finish: set `finished_at`, delete sets never logged, redirect home
   - [ ] Discard: delete the session and its sets, no trace, program position unchanged
4. Prefill Last finished session for that exercise in that workout fills the inputs. First real logic beyond load/save — introduce a `WorkoutSessionService` here.
   - [ ] Fill reps and weight by set number from the last finished session of that exercise in that workout
   - [ ] More sets than last time → extra sets copy the last set's values
   - [ ] Show the rep range next to each exercise while logging
5. Rest timer Auto-start on logging a set. End timestamp stored, recomputed on return; Wake Lock while running.
   - [ ] Store the end timestamp when a set is logged (exercise's rest seconds from now)
   - [ ] Countdown on the session page, recomputed from the stored timestamp on reload/return
   - [ ] Wake Lock while running
   - [ ] On zero: visual only (no sound or vibration)
6. Programs *(partly done)* Ordered sequence of workouts and rest days, one active, position advancing on finish or skip, wrapping at the end.
   - [x] Program CRUD, program days, activate (one active per user)
   - [ ] Finishing a program session advances position, wrapping to day 0 and incrementing the rotation count
   - [ ] Skip: advance position, record nothing
   - [ ] Rest days auto-advance
   - [ ] Hide or disable Start on rest days
7. Progression suggestion Inline prompt when every target set hit the top of the range last time.
   - [ ] Working weight updates automatically on finish: the top set's weight from that session
   - [ ] On the session page, next to the exercise: suggest increasing weight when all target sets (the workout's set count) reached the top of the rep range last session
8. Stall detection N sessions with no change, configurable.
   - [ ] Stall threshold N per user, default 3, on a settings page
   - [ ] On the session page, next to the exercise: notice when the exercise has stalled (definition under behaviour)
9. Deload Rotation counting, triggering, empty fields with working weight shown, no numbers stored.
   - [ ] Every N rotations (program's deload interval) the next session is a deload
   - [ ] Deload session: no prefill, fields empty, working weight shown as reference; you choose the weight
   - [ ] Finishing a deload stores no sets; it only advances position and the rotation counter; working weight unchanged
10. Export / import JSON for workouts and programs, text paste and file upload, imports land inactive. Last: least core.
    - [ ] Export a workout or program as JSON
    - [ ] Import by text paste or file upload; imports land inactive
    - [ ] Name already exists → replace the existing one

Steps 1–3 give you something you'd actually use. Steps 4–6 make it nicer than a spreadsheet at the gym. Steps 7–9 all read session history; build them together in the session service.

No progression chart: stall detection and the progression suggestion answer "am I progressing?".

Switch from editing `V1__init.sql` to `V2__` migrations once you start logging real sessions (likely around step 6).

## Requirements — structure

**Program**

Named, ordered sequence of workouts and rest days; wraps at the end. One active at a time. Tracks current position and rotation count. Deload interval set per program.

**Workout**

Named list of exercises, plus a colour. Standalone and reusable across programs.

**Exercise in a workout**

Free text name, target sets, one rep range shared by all sets, rest seconds, notes. Working weight tracked per exercise *per workout*, so the same lift progresses independently in different workouts. Working weight updates automatically on finish to that session's top set weight.

**Session**

One logged instance of a workout on a date. Started from the active program's current day, or from any workout outside a program. In progress until finished; immutable and hidden once finished. Can be discarded while in progress, leaving no trace.

**Set**

Reps and weight, editable and deletable while the session is in progress. Bodyweight is 0 kg. The number of sets per exercise can change during a session; optionally the change is saved back to the workout (not the default). Sets never logged are deleted on finish.

**Deload**

Triggered after N rotations. Weight and rep fields start empty with the working weight shown as reference; the weight is chosen by hand. No numbers stored, so it never affects prefill, progression, stall detection or working weight. Rotation counter still advances.

## Requirements — behaviour

- Active program presents the next entry; do it or skip it, position advances either way. Skipping records nothing. Rest days auto-advance.
- Sets prefill from the last finished non-deload session of that exercise in that workout. More sets than last time → extra sets copy the last set.
- Rep range visible while logging.
- All target sets at or above the top of the range → inline suggestion to increase weight next session. Advisory only. "Target sets" = the exercise's set count in the workout; extra sets don't count, missing sets mean no suggestion.
- Per exercise per workout, stall detection compares the top set of the latest finished session with the top set N sessions back (N default 3, configurable per user). Progress = higher weight, or the same weight with more reps; otherwise inform that progression has stalled. Top sets are computed from the logged sets, not stored. Comparing only the two ends means a one-off typo in between can't cause a false stall.
- Typos need no history view: working weight is overwritten every session, and the next session's prefill shows the bad value so it gets corrected at the gym.
- Progression suggestion and stall notice appear on the session page, next to the exercise.
- Rest timer starts automatically on logging a set. Stores an end timestamp and recomputes on return; Wake Lock keeps the screen on while running. Timer end is visual only.
- Session exercise list is fixed from the workout.

## Out of scope for v1

- Viewing or editing finished sessions
- Decrease-weight suggestions
- Personal bests
- Time-based and cardio exercises
- Unplanned exercises mid-session
- History export
- Progression chart

## Still undecided

- UI direction — dedicated screens vs. the spreadsheet-style grid
- Editing a program while it's active, and what happens to position
- Import replace details — diff-before-apply or not; how exercises are matched (by name?) so history and prefill survive the replace

## Stack notes

- Dependencies: Spring Web, Thymeleaf, Spring Data JPA, Flyway, PostgreSQL Driver. Optional: Lombok, DevTools.
- Flyway owns the schema; `ddl-auto=validate` so Hibernate only checks entities match.
- `show-sql=true` and `format_sql=true` on from day one.
- Own Postgres container and volume — not the one that gets `down -v`.
- Edit `V1__init.sql` freely and reset while there's no real data; switch to `V2__` onward once you're logging real sessions.

Working doc — expect this to change as decisions land.
