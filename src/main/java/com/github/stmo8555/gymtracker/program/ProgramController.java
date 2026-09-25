package com.github.stmo8555.gymtracker.program;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.github.stmo8555.gymtracker.user.MockUserProvider;

@Controller
@RequestMapping("/programs")
public class ProgramController {

    private final ProgramRepository repo;
    private final MockUserProvider mockUser;

    public ProgramController(ProgramRepository repo, MockUserProvider mockUser) {
        this.repo = repo;
        this.mockUser = mockUser;
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
        return "program-edit";
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
}
