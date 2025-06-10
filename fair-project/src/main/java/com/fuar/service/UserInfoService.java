package com.fuar.service;

import com.fuar.model.User;
import com.fuar.model.UserInfo;
import com.fuar.model.Skill;
import com.fuar.repository.UserRepository;
import com.fuar.repository.UserInfoRepository;
import com.fuar.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserInfoService {
    private final UserInfoRepository userInfoRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;

    public UserInfo getUserInfo(Long userId) {
        return userInfoRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("UserInfo not found"));
    }

    /**
     * Check if a user has UserInfo record, create if not exists
     * @param userId User ID to check
     * @return true if created, false if already exists
     */
    @Transactional
    public boolean createUserInfoIfNotExists(Long userId) {
        try {
            // Check if user already has UserInfo
            if (userInfoRepository.findByUser_Id(userId).isPresent()) {
                System.out.println("UserInfo already exists for user " + userId);
                return false;
            }

            // User does not have UserInfo, need to create one
            // First, get the user from repository
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
            
            // Check if user already has a UserInfo reference
            if (user.getUserInfo() != null) {
                System.out.println("User already has UserInfo reference: " + user.getUserInfo().getId());
                return false;
            }
            
            // Create new UserInfo
            UserInfo newUserInfo = new UserInfo();
            newUserInfo.setUser(user);
            
            // Initialize empty collections
            newUserInfo.setSkills(new HashSet<>());
            
            // Save the new UserInfo - don't set the ID manually, let the database handle it
            UserInfo savedUserInfo = userInfoRepository.save(newUserInfo);
            System.out.println("Created new UserInfo with id: " + savedUserInfo.getId());
            
            // Update the user with the new UserInfo
            user.setUserInfo(savedUserInfo);
            userRepository.save(user);
            System.out.println("Updated user " + userId + " with new UserInfo " + savedUserInfo.getId());
            
            return true;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // This is likely a duplicate key violation
            System.err.println("Data integrity violation when creating UserInfo for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            
            // Try to diagnose the issue
            try {
                // Check if the user already has a UserInfo in the database
                var existingUserInfo = userInfoRepository.findByUser_Id(userId);
                if (existingUserInfo.isPresent()) {
                    // There's already a UserInfo for this user, update the user reference
                    User user = userRepository.findById(userId).orElse(null);
                    if (user != null && user.getUserInfo() == null) {
                        user.setUserInfo(existingUserInfo.get());
                        userRepository.save(user);
                        System.out.println("Fixed UserInfo reference for user " + userId);
                        return true;
                    }
                }
            } catch (Exception diagEx) {
                System.err.println("Error during diagnosis: " + diagEx.getMessage());
            }
            
            return false;
        } catch (Exception e) {
            System.err.println("Error creating UserInfo for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Transactional
    public UserInfo updateUserInfo(Long userId, UserInfo userInfoDetails) {
        // First, ensure UserInfo exists for this user
        try {
            // Try to get existing UserInfo
            UserInfo userInfo = getUserInfo(userId);
            
            // Update fields
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
        } catch (RuntimeException ex) {
            // UserInfo not found, create a new one
            if (ex.getMessage().contains("UserInfo not found")) {
                // Create new UserInfo
                createUserInfoIfNotExists(userId);
                
                // Now get the newly created UserInfo
                UserInfo newUserInfo = getUserInfo(userId);
                
                // Update fields
                newUserInfo.setBio(userInfoDetails.getBio());
                newUserInfo.setHeadLine(userInfoDetails.getHeadLine());
                newUserInfo.setLocation(userInfoDetails.getLocation());
                newUserInfo.setCountry(userInfoDetails.getCountry());
                newUserInfo.setLinkedinUrl(userInfoDetails.getLinkedinUrl());
                newUserInfo.setGithubUrl(userInfoDetails.getGithubUrl());
                newUserInfo.setPersonalWebsite(userInfoDetails.getPersonalWebsite());
                newUserInfo.setTwitterUrl(userInfoDetails.getTwitterUrl());
                newUserInfo.setInstagramUrl(userInfoDetails.getInstagramUrl());
                newUserInfo.setFacebookUrl(userInfoDetails.getFacebookUrl());
                newUserInfo.setYoutubeUrl(userInfoDetails.getYoutubeUrl());
                newUserInfo.setMediumUrl(userInfoDetails.getMediumUrl());
                newUserInfo.setScholarUrl(userInfoDetails.getScholarUrl());
                newUserInfo.setResearchGateUrl(userInfoDetails.getResearchGateUrl());
                newUserInfo.setOrcidId(userInfoDetails.getOrcidId());
                
                return userInfoRepository.save(newUserInfo);
            } else {
                // Some other error occurred
                throw ex;
            }
        }
    }

    @Transactional
    public UserInfo updateUserInfoFields(Long userId, com.fuar.dto.UserInfoDTO userInfoDTO) {
        // Get existing UserInfo - at this point it should exist
        UserInfo userInfo = userInfoRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("UserInfo not found after creation attempt"));
        
        // Log incoming DTO data
        System.out.println("Updating UserInfo for user " + userId);
        System.out.println("DTO received: " + userInfoDTO);
        System.out.println("Bio value: '" + userInfoDTO.getBio() + "'");
        System.out.println("HeadLine value: '" + userInfoDTO.getHeadLine() + "'");
        
        // Update fields from DTO
        userInfo.setBio(userInfoDTO.getBio());
        userInfo.setHeadLine(userInfoDTO.getHeadLine());
        userInfo.setLocation(userInfoDTO.getLocation());
        userInfo.setCountry(userInfoDTO.getCountry());
        userInfo.setLinkedinUrl(userInfoDTO.getLinkedinUrl());
        userInfo.setGithubUrl(userInfoDTO.getGithubUrl());
        userInfo.setPersonalWebsite(userInfoDTO.getPersonalWebsite());
        userInfo.setTwitterUrl(userInfoDTO.getTwitterUrl());
        userInfo.setInstagramUrl(userInfoDTO.getInstagramUrl());
        userInfo.setFacebookUrl(userInfoDTO.getFacebookUrl());
        userInfo.setYoutubeUrl(userInfoDTO.getYoutubeUrl());
        userInfo.setMediumUrl(userInfoDTO.getMediumUrl());
        userInfo.setScholarUrl(userInfoDTO.getScholarUrl());
        userInfo.setResearchGateUrl(userInfoDTO.getResearchGateUrl());
        userInfo.setOrcidId(userInfoDTO.getOrcidId());
        
        // Save and return the updated entity
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
