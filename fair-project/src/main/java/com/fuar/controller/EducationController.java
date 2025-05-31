package com.fuar.controller;

import com.fuar.dto.EducationDTO;
import com.fuar.model.Education;
import com.fuar.service.EducationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/education")
@RequiredArgsConstructor
public class EducationController {
    private final EducationService educationService;

    @GetMapping("/user/{userInfoId}")
    public ResponseEntity<List<EducationDTO>> getUserEducation(@PathVariable Long userInfoId) {
        return ResponseEntity.ok(educationService.getUserEducation(userInfoId));
    }

    @PostMapping("/user/{userInfoId}")
    @PreAuthorize("@userInfoService.getUserInfo(#userInfoId).user.id == authentication.principal.id")
    public ResponseEntity<Education> addEducation(
            @PathVariable Long userInfoId,
            @RequestBody EducationDTO educationDTO
    ) {
        Education education = new Education();
        education.setInstitution(educationDTO.getInstitution());
        education.setFieldOfStudy(educationDTO.getFieldOfStudy());
        education.setDescription(educationDTO.getDescription());
        education.setStartDate(educationDTO.getStartDate());
        education.setEndDate(educationDTO.getEndDate());
        education.setCurrent(educationDTO.isCurrent());
        education.setGrade(educationDTO.getGrade());
        education.setActivities(educationDTO.getActivities());

        return ResponseEntity.ok(educationService.addEducation(userInfoId, education));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@educationService.getUserEducation(#id).get(0).userInfo.user.id == authentication.principal.id")
    public ResponseEntity<Education> updateEducation(
            @PathVariable Long id,
            @RequestBody EducationDTO educationDTO
    ) {
        Education education = new Education();
        education.setInstitution(educationDTO.getInstitution());
        education.setFieldOfStudy(educationDTO.getFieldOfStudy());
        education.setDescription(educationDTO.getDescription());
        education.setStartDate(educationDTO.getStartDate());
        education.setEndDate(educationDTO.getEndDate());
        education.setCurrent(educationDTO.isCurrent());
        education.setGrade(educationDTO.getGrade());
        education.setActivities(educationDTO.getActivities());

        return ResponseEntity.ok(educationService.updateEducation(id, education));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@educationService.getUserEducation(#id).get(0).userInfo.user.id == authentication.principal.id")
    public ResponseEntity<Void> deleteEducation(@PathVariable Long id) {
        educationService.deleteEducation(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Education>> searchEducation(@RequestParam String keyword) {
        return ResponseEntity.ok(educationService.searchEducation(keyword));
    }

    @GetMapping("/institution/{institution}/field/{field}")
    public ResponseEntity<List<Education>> findByInstitutionAndField(
            @PathVariable String institution,
            @PathVariable String field
    ) {
        return ResponseEntity.ok(educationService.findByInstitutionAndField(institution, field));
    }
}
