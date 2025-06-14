package com.fuar.controller;

import com.fuar.dto.WorkExperienceDTO;
import com.fuar.model.WorkExperience;
import com.fuar.service.WorkExperienceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/work-experience")
@RequiredArgsConstructor
public class WorkExperienceController {
    private final WorkExperienceService workExperienceService;

    @GetMapping("/user/{userInfoId}")
    public ResponseEntity<List<WorkExperience>> getUserWorkExperiences(@PathVariable Long userInfoId) {
        return ResponseEntity.ok(workExperienceService.getUserWorkExperiences(userInfoId));
    }

    @PostMapping("/user/{userInfoId}")
    @PreAuthorize("@userInfoService.getUserInfo(#userInfoId).user.id == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<WorkExperience> addWorkExperience(
            @PathVariable Long userInfoId,
            @RequestBody WorkExperienceDTO workExperienceDTO
    ) {
        WorkExperience workExperience = new WorkExperience();
        // Map DTO to entity
        workExperience.setCompany(workExperienceDTO.getCompany());
        workExperience.setPosition(workExperienceDTO.getPosition());
        workExperience.setDescription(workExperienceDTO.getDescription());
        workExperience.setStartDate(workExperienceDTO.getStartDate());
        workExperience.setEndDate(workExperienceDTO.getEndDate());
        workExperience.setCurrent(workExperienceDTO.isCurrent());
        workExperience.setLocation(workExperienceDTO.getLocation());
        workExperience.setCompanyUrl(workExperienceDTO.getCompanyUrl());

        return ResponseEntity.ok(workExperienceService.addWorkExperience(userInfoId, workExperience));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@workExperienceService.getWorkExperienceById(#id).userInfo.user.id == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<WorkExperience> updateWorkExperience(
            @PathVariable Long id,
            @RequestBody WorkExperienceDTO workExperienceDTO
    ) {
        WorkExperience workExperience = new WorkExperience();
        // Map DTO to entity
        workExperience.setCompany(workExperienceDTO.getCompany());
        workExperience.setPosition(workExperienceDTO.getPosition());
        workExperience.setDescription(workExperienceDTO.getDescription());
        workExperience.setStartDate(workExperienceDTO.getStartDate());
        workExperience.setEndDate(workExperienceDTO.getEndDate());
        workExperience.setCurrent(workExperienceDTO.isCurrent());
        workExperience.setLocation(workExperienceDTO.getLocation());
        workExperience.setCompanyUrl(workExperienceDTO.getCompanyUrl());

        return ResponseEntity.ok(workExperienceService.updateWorkExperience(id, workExperience));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@workExperienceService.getWorkExperienceById(#id).userInfo.user.id == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteWorkExperience(@PathVariable Long id) {
        workExperienceService.deleteWorkExperience(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<WorkExperience>> searchWorkExperiences(@RequestParam String keyword) {
        return ResponseEntity.ok(workExperienceService.searchWorkExperiences(keyword));
    }

    @GetMapping("/company/{company}/current")
    public ResponseEntity<List<WorkExperience>> getCurrentWorkExperiencesByCompany(@PathVariable String company) {
        return ResponseEntity.ok(workExperienceService.findCurrentWorkExperiencesByCompany(company));
    }
}
