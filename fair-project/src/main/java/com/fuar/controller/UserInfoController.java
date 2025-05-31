package com.fuar.controller;

import com.fuar.dto.UserInfoDTO;
import com.fuar.model.UserInfo;
import com.fuar.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user-info")
@RequiredArgsConstructor
public class UserInfoController {
    private final UserInfoService userInfoService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserInfo> getUserInfo(@PathVariable Long userId) {
        return ResponseEntity.ok(userInfoService.getUserInfo(userId));
    }

    @PutMapping("/{userId}")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<UserInfo> updateUserInfo(
            @PathVariable Long userId,
            @RequestBody UserInfoDTO userInfoDTO
    ) {
        UserInfo userInfo = new UserInfo();
        // Map DTO to entity
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

        return ResponseEntity.ok(userInfoService.updateUserInfo(userId, userInfo));
    }

    @PostMapping("/{userId}/skills/{skillId}")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<UserInfo> addSkill(
            @PathVariable Long userId,
            @PathVariable Long skillId
    ) {
        return ResponseEntity.ok(userInfoService.addSkillToUserInfo(userId, skillId));
    }

    @DeleteMapping("/{userId}/skills/{skillId}")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<UserInfo> removeSkill(
            @PathVariable Long userId,
            @PathVariable Long skillId
    ) {
        return ResponseEntity.ok(userInfoService.removeSkillFromUserInfo(userId, skillId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserInfo>> searchUserInfos(@RequestParam String keyword) {
        return ResponseEntity.ok(userInfoService.searchUserInfos(keyword));
    }

    @GetMapping("/by-skill/{skillName}")
    public ResponseEntity<List<UserInfo>> getUsersBySkill(@PathVariable String skillName) {
        return ResponseEntity.ok(userInfoService.findUsersBySkill(skillName));
    }
}
