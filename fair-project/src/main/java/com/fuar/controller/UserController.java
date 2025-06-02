package com.fuar.controller;

import com.fuar.model.Role;
import com.fuar.model.User;
import com.fuar.model.UserInfo;
import com.fuar.repository.UserRepository;
import com.fuar.service.UserInfoService;
import com.fuar.service.EducationService;
import com.fuar.service.WorkExperienceService;
import com.fuar.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final UserInfoService userInfoService;
    private final EducationService educationService;
    private final WorkExperienceService workExperienceService;
    private final SkillService skillService;

    /**
     * Get all users
     * @param role Optional role filter
     * @return List of users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(@RequestParam(required = false) Role role) {
        try {
            System.out.println("GET /api/v1/users çağrıldı");
            List<User> users;
            
            if (role != null) {
                // Filter users by role if specified
                users = userRepository.findAll().stream()
                    .filter(user -> user.getRole() == role)
                    .collect(Collectors.toList());
                System.out.println(role + " rolüne sahip " + users.size() + " kullanıcı bulundu");
            } else {
                // Get all users
                users = userRepository.findAll();
                System.out.println("Toplam " + users.size() + " kullanıcı bulundu");
            }
            
            // Remove sensitive information before returning
            users.forEach(user -> user.setPassword(null));
            
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            System.err.println("Kullanıcılar alınırken hata oluştu: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get user by ID
     * @param id User ID
     * @return User if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        try {
            System.out.println("GET /api/v1/users/" + id + " çağrıldı");
            Optional<User> userOpt = userRepository.findById(id);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setPassword(null); // Remove sensitive information
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Kullanıcı alınırken hata oluştu: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all speakers (users with SPEAKER role)
     * @return List of speakers
     */
    @GetMapping("/speakers")
    public ResponseEntity<List<User>> getAllSpeakers() {
        try {
            System.out.println("GET /api/v1/users/speakers çağrıldı");
            List<User> speakers = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.SPEAKER)
                .collect(Collectors.toList());
                
            System.out.println(speakers.size() + " konuşmacı bulundu");
            
            // Remove sensitive information before returning
            speakers.forEach(user -> user.setPassword(null));
            
            return ResponseEntity.ok(speakers);
        } catch (Exception e) {
            System.err.println("Konuşmacılar alınırken hata oluştu: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get the current authenticated user's profile with all related information
     * @return Complete user profile data
     */
    @GetMapping("/me/profile")
    public ResponseEntity<?> getCurrentUserProfile() {
    try {
        // Get the authenticated user from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        
        // Get user email from authentication
        String email = authentication.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }
        
        User user = userOpt.get();
        Long userId = user.getId();
        
        // Create response map
        Map<String, Object> response = new HashMap<>();
        
        // Basic user info
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("name", user.getName());
        userMap.put("email", user.getEmail());
        userMap.put("image", user.getImage());
        userMap.put("role", user.getRole());
        response.put("user", userMap);
        
        try {
            // Get user detailed info
            UserInfo userInfo = userInfoService.getUserInfo(userId);
            response.put("userInfo", userInfo);
            
            // Get user education
            response.put("education", educationService.getUserEducation(userId));
            
            // Get work experience
            response.put("workExperience", workExperienceService.getUserWorkExperiences(userId));
            
            // Get skills
            response.put("skills", userInfo.getSkills());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // If user info not found, return just basic user data
            System.err.println("User profile details retrieval error: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    } catch (Exception e) {
        System.err.println("Kullanıcı profili alınırken hata oluştu: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.internalServerError().body("An error occurred while fetching user profile");
    }
  }
}
