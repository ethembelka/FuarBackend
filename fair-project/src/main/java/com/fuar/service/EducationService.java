package com.fuar.service;

import com.fuar.model.Education;
import com.fuar.model.UserInfo;
import com.fuar.dto.EducationDTO;
import com.fuar.repository.EducationRepository;
import com.fuar.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EducationService {
    private final EducationRepository educationRepository;
    private final UserInfoRepository userInfoRepository;

    public List<EducationDTO> getUserEducation(Long userId) {
        UserInfo userInfo = userInfoRepository.findByUser_Id(userId)
            .orElseThrow(() -> new RuntimeException("UserInfo not found for user: " + userId));
        return educationRepository.findByUserInfo_IdOrderByStartDateDesc(userInfo.getId())
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public Education addEducation(Long userInfoId, Education education) {
        UserInfo userInfo = userInfoRepository.findById(userInfoId)
                .orElseThrow(() -> new RuntimeException("UserInfo not found"));

        education.setUserInfo(userInfo);
        return educationRepository.save(education);
    }

    @Transactional
    public Education updateEducation(Long id, Education educationDetails) {
        Education education = educationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Education not found"));

        education.setInstitution(educationDetails.getInstitution());
        education.setFieldOfStudy(educationDetails.getFieldOfStudy());
        education.setDescription(educationDetails.getDescription());
        education.setStartDate(educationDetails.getStartDate());
        education.setEndDate(educationDetails.getEndDate());
        education.setCurrent(educationDetails.isCurrent());
        education.setGrade(educationDetails.getGrade());
        education.setActivities(educationDetails.getActivities());

        return educationRepository.save(education);
    }

    public void deleteEducation(Long id) {
        if (!educationRepository.existsById(id)) {
            throw new RuntimeException("Education not found");
        }
        educationRepository.deleteById(id);
    }

    public Education getEducationById(Long id) {
        return educationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Education not found with id: " + id));
    }

    public List<Education> searchEducation(String keyword) {
        return educationRepository.searchByKeyword(keyword);
    }

    public List<Education> findByInstitutionAndField(String institution, String fieldOfStudy) {
        return educationRepository.findByInstitutionAndFieldOfStudy(institution, fieldOfStudy);
    }

    private EducationDTO convertToDTO(Education education) {
        return EducationDTO.builder()
            .id(education.getId())
            .institution(education.getInstitution())
            .fieldOfStudy(education.getFieldOfStudy())
            .description(education.getDescription())
            .startDate(education.getStartDate())
            .endDate(education.getEndDate())
            .current(education.isCurrent())
            .grade(education.getGrade())
            .activities(education.getActivities())
            .build();
    }
}
