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
        try {
            return ResponseEntity.ok(userInfoService.getUserInfo(userId));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("UserInfo not found")) {
                // Try to create a new UserInfo for this user
                boolean created = userInfoService.createUserInfoIfNotExists(userId);
                if (created) {
                    // Successfully created, now return it
                    return ResponseEntity.ok(userInfoService.getUserInfo(userId));
                }
            }
            // If we couldn't create or some other error occurred, rethrow
            throw e;
        }
    }

    @PutMapping("/{userId}")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<UserInfo> updateUserInfo(
            @PathVariable Long userId,
            @RequestBody UserInfoDTO userInfoDTO
    ) {
        try {
            // First check if the user's UserInfo record exists
            boolean created = false;
            try {
                userInfoService.getUserInfo(userId);
            } catch (RuntimeException e) {
                if (e.getMessage().contains("UserInfo not found")) {
                    // UserInfo record doesn't exist, create it
                    System.out.println("UserInfo not found for user " + userId + ", attempting to create one");
                    created = userInfoService.createUserInfoIfNotExists(userId);
                    if (!created) {
                        System.err.println("Failed to create UserInfo for user " + userId);
                        return ResponseEntity.status(500)
                            .body(null); // Creation failed
                    }
                    System.out.println("Successfully created UserInfo for user " + userId);
                } else {
                    throw e; // Some other error
                }
            }
            
            // Now send the DTO values to the service
            try {
                UserInfo updatedInfo = userInfoService.updateUserInfoFields(userId, userInfoDTO);
                return ResponseEntity.ok(updatedInfo);
            } catch (Exception e) {
                System.err.println("Error updating UserInfo for user " + userId + ": " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(500)
                    .body(null);
            }
        } catch (Exception e) {
            System.err.println("Unexpected error in updateUserInfo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(null);
        }
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
