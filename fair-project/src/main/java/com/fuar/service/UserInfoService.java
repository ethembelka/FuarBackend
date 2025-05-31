package com.fuar.service;

import com.fuar.model.UserInfo;
import com.fuar.model.Skill;
import com.fuar.repository.UserInfoRepository;
import com.fuar.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserInfoService {
    private final UserInfoRepository userInfoRepository;
    private final SkillRepository skillRepository;

    public UserInfo getUserInfo(Long userId) {
        return userInfoRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("UserInfo not found"));
    }

    @Transactional
    public UserInfo updateUserInfo(Long userId, UserInfo userInfoDetails) {
        UserInfo userInfo = getUserInfo(userId);
        
        userInfo.setBio(userInfoDetails.getBio());
        userInfo.setHeadLine(userInfoDetails.getHeadLine());
        userInfo.setLocation(userInfoDetails.getLocation());
        userInfo.setCountry(userInfoDetails.getCountry());
        userInfo.setLinkedinUrl(userInfoDetails.getLinkedinUrl());
        userInfo.setGithubUrl(userInfoDetails.getGithubUrl());
        userInfo.setPersonalWebsite(userInfoDetails.getPersonalWebsite());
        userInfo.setTwitterUrl(userInfoDetails.getTwitterUrl());
        userInfo.setInstagramUrl(userInfoDetails.getInstagramUrl());
        userInfo.setFacebookUrl(userInfoDetails.getFacebookUrl());
        userInfo.setYoutubeUrl(userInfoDetails.getYoutubeUrl());
        userInfo.setMediumUrl(userInfoDetails.getMediumUrl());
        userInfo.setScholarUrl(userInfoDetails.getScholarUrl());
        userInfo.setResearchGateUrl(userInfoDetails.getResearchGateUrl());
        userInfo.setOrcidId(userInfoDetails.getOrcidId());

        return userInfoRepository.save(userInfo);
    }

    @Transactional
    public UserInfo addSkillToUserInfo(Long userId, Long skillId) {
        UserInfo userInfo = getUserInfo(userId);
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        userInfo.getSkills().add(skill);
        return userInfoRepository.save(userInfo);
    }

    @Transactional
    public UserInfo removeSkillFromUserInfo(Long userId, Long skillId) {
        UserInfo userInfo = getUserInfo(userId);
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        userInfo.getSkills().remove(skill);
        return userInfoRepository.save(userInfo);
    }

    public List<UserInfo> searchUserInfos(String keyword) {
        return userInfoRepository.searchByKeyword(keyword);
    }

    public List<UserInfo> findUsersBySkill(String skillName) {
        return userInfoRepository.findBySkillName(skillName);
    }
}
