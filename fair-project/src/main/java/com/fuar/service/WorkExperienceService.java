package com.fuar.service;

import com.fuar.model.WorkExperience;
import com.fuar.model.UserInfo;
import com.fuar.repository.WorkExperienceRepository;
import com.fuar.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkExperienceService {
    private final WorkExperienceRepository workExperienceRepository;
    private final UserInfoRepository userInfoRepository;

    public List<WorkExperience> getUserWorkExperiences(Long userId) {
        UserInfo userInfo = userInfoRepository.findByUser_Id(userId)
            .orElseThrow(() -> new RuntimeException("UserInfo not found for user: " + userId));
        return workExperienceRepository.findByUserInfo_IdOrderByStartDateDesc(userInfo.getId());
    }

    @Transactional
    public WorkExperience addWorkExperience(Long userInfoId, WorkExperience workExperience) {
        UserInfo userInfo = userInfoRepository.findById(userInfoId)
                .orElseThrow(() -> new RuntimeException("UserInfo not found"));

        workExperience.setUserInfo(userInfo);
        return workExperienceRepository.save(workExperience);
    }

    @Transactional
    public WorkExperience updateWorkExperience(Long id, WorkExperience workExperienceDetails) {
        WorkExperience workExperience = workExperienceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Work experience not found"));

        workExperience.setCompany(workExperienceDetails.getCompany());
        workExperience.setPosition(workExperienceDetails.getPosition());
        workExperience.setDescription(workExperienceDetails.getDescription());
        workExperience.setStartDate(workExperienceDetails.getStartDate());
        workExperience.setEndDate(workExperienceDetails.getEndDate());
        workExperience.setCurrent(workExperienceDetails.isCurrent());
        workExperience.setLocation(workExperienceDetails.getLocation());
        workExperience.setCompanyUrl(workExperienceDetails.getCompanyUrl());

        return workExperienceRepository.save(workExperience);
    }

    public void deleteWorkExperience(Long id) {
        if (!workExperienceRepository.existsById(id)) {
            throw new RuntimeException("Work experience not found");
        }
        workExperienceRepository.deleteById(id);
    }

    public List<WorkExperience> searchWorkExperiences(String keyword) {
        return workExperienceRepository.searchByKeyword(keyword);
    }

    public List<WorkExperience> findCurrentWorkExperiencesByCompany(String company) {
        return workExperienceRepository.findByCompanyAndCurrent(company, true);
    }
}
