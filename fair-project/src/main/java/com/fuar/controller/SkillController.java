package com.fuar.controller;

import com.fuar.dto.SkillDTO;
import com.fuar.model.Skill;
import com.fuar.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/skills")
@RequiredArgsConstructor
public class SkillController {
    private final SkillService skillService;

    @GetMapping
    public ResponseEntity<List<Skill>> getAllSkills() {
        return ResponseEntity.ok(skillService.getAllSkills());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Skill> getSkillById(@PathVariable Long id) {
        return ResponseEntity.ok(skillService.getSkillById(id));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Skill> getSkillByName(@PathVariable String name) {
        return ResponseEntity.ok(skillService.getSkillByName(name));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Skill> createSkill(@RequestBody SkillDTO skillDTO) {
        Skill skill = new Skill();
        skill.setName(skillDTO.getName());
        skill.setDescription(skillDTO.getDescription());
        return ResponseEntity.ok(skillService.createSkill(skill));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Skill> updateSkill(
            @PathVariable Long id,
            @RequestBody SkillDTO skillDTO
    ) {
        Skill skill = new Skill();
        skill.setName(skillDTO.getName());
        skill.setDescription(skillDTO.getDescription());
        return ResponseEntity.ok(skillService.updateSkill(id, skill));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        skillService.deleteSkill(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Skill>> searchSkills(@RequestParam String keyword) {
        return ResponseEntity.ok(skillService.searchSkills(keyword));
    }

    @GetMapping("/popular")
    public ResponseEntity<List<Skill>> getMostPopularSkills() {
        return ResponseEntity.ok(skillService.getMostPopularSkills());
    }

    @GetMapping("/user/{userInfoId}")
    public ResponseEntity<List<Skill>> getUserSkills(@PathVariable Long userInfoId) {
        return ResponseEntity.ok(skillService.getUserSkills(userInfoId));
    }
}
