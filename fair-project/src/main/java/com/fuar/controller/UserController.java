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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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

  /**
   * Upload a profile image for a user
   * @param userId User ID
   * @param file The image file to upload
   * @return Updated user with image URL
   */
  @PostMapping("/{userId}/profile-image")
  public ResponseEntity<?> uploadProfileImage(
      @PathVariable Long userId,
      @RequestParam("file") MultipartFile file
  ) {
    try {
      // Check if user exists
      Optional<User> userOpt = userRepository.findById(userId);
      if (userOpt.isEmpty()) {
        return ResponseEntity.status(404).body("User not found");
      }

      User user = userOpt.get();
      
      // Verify authorization - only allow users to upload their own profile image
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication == null || !authentication.isAuthenticated() || 
          !authentication.getName().equals(user.getEmail())) {
        return ResponseEntity.status(403).body("Not authorized to update this user's profile image");
      }
      
      // Validate the file
      if (file.isEmpty()) {
        return ResponseEntity.badRequest().body("File is empty");
      }
      
      // Check file type
      String contentType = file.getContentType();
      if (contentType == null || !contentType.startsWith("image/")) {
        return ResponseEntity.badRequest().body("Only image files are allowed");
      }
      
      // Limit file size (5MB)
      if (file.getSize() > 5 * 1024 * 1024) {
        return ResponseEntity.badRequest().body("File size exceeds maximum limit of 5MB");
      }
      
      // Generate a unique filename
      String originalFilename = file.getOriginalFilename();
      String extension = originalFilename != null && originalFilename.contains(".") ? 
          originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
      String filename = "profile_" + userId + "_" + System.currentTimeMillis() + extension;
      
      // Get project root directory for primary storage location
      String projectRoot = System.getProperty("user.dir");
      String projectUploadDir = projectRoot + "/uploads/profiles/";
      
      // Define alternative upload directories as fallbacks
      String[] uploadDirs = {
          projectUploadDir, // Project directory (primary)
          System.getProperty("java.io.tmpdir") + "/uploads/profiles/", // Temp directory
          System.getProperty("user.home") + "/uploads/profiles/", // Home directory
          "uploads/profiles/" // Relative directory
      };
      
      // Try to save file in each directory until successful
      File savedFile = null;
      String usedDirectory = null;
      Exception lastException = null;
      
      for (String dir : uploadDirs) {
          File directory = new File(dir);
          
          // Create directory if it doesn't exist
          if (!directory.exists()) {
              try {
                  boolean created = directory.mkdirs();
                  if (created) {
                      System.out.println("Created directory: " + directory.getAbsolutePath());
                      
                      // Try to make directory world-readable on Unix systems
                      try {
                          Process process = Runtime.getRuntime().exec("chmod 755 " + directory.getAbsolutePath());
                          int exitCode = process.waitFor();
                          if (exitCode == 0) {
                              System.out.println("Set permissions for directory: " + directory.getAbsolutePath());
                          }
                      } catch (Exception e) {
                          // Ignore permission setting errors, just log them
                          System.out.println("Could not set permissions: " + e.getMessage());
                      }
                  } else {
                      System.err.println("Failed to create directory: " + directory.getAbsolutePath());
                      continue; // Skip to next directory
                  }
              } catch (Exception e) {
                  System.err.println("Error creating directory " + directory.getAbsolutePath() + ": " + e.getMessage());
                  continue; // Skip to next directory
              }
          }
          
          // Check if directory is writable
          if (!directory.canWrite()) {
              System.err.println("Directory not writable: " + directory.getAbsolutePath());
              continue; // Skip to next directory
          }
          
          File targetFile = new File(directory, filename);
          
          try {
              // Try to save the file
              file.transferTo(targetFile);
              
              // Verify file was saved
              if (targetFile.exists() && targetFile.length() > 0) {
                  System.out.println("File saved successfully to: " + targetFile.getAbsolutePath());
                  savedFile = targetFile;
                  usedDirectory = dir;
                  break; // Successfully saved, exit the loop
              } else {
                  System.err.println("File saving verification failed: " + targetFile.getAbsolutePath());
              }
          } catch (Exception e) {
              System.err.println("Error saving to " + targetFile.getAbsolutePath() + ": " + e.getMessage());
              lastException = e;
          }
      }
      
      // If all attempts failed, return an error
      if (savedFile == null) {
          String errorMsg = "Failed to save file to any location";
          if (lastException != null) {
              errorMsg += ": " + lastException.getMessage();
          }
          System.err.println(errorMsg);
          return ResponseEntity.status(500).body(errorMsg);
      }
      
      // Clean up old profile image if it exists
      String oldImagePath = user.getImage();
      if (oldImagePath != null && !oldImagePath.isEmpty()) {
          if (oldImagePath.contains("/")) {
              // Extract the filename from the path
              String oldFilename = oldImagePath.substring(oldImagePath.lastIndexOf("/") + 1);
              
              // Try to delete from all possible locations
              for (String dir : uploadDirs) {
                  File oldFile = new File(dir, oldFilename);
                  if (oldFile.exists() && oldFile.isFile()) {
                      boolean deleted = oldFile.delete();
                      if (deleted) {
                          System.out.println("Successfully deleted old image: " + oldFile.getAbsolutePath());
                      } else {
                          System.err.println("Failed to delete old image: " + oldFile.getAbsolutePath());
                      }
                  }
              }
          }
      }
      
      // Update the user's image field with the relative path for URL construction
      String imagePath = "/uploads/profiles/" + filename;
      user.setImage(imagePath);
      userRepository.save(user);
      
      System.out.println("Profile image updated successfully:");
      System.out.println("- File saved to: " + savedFile.getAbsolutePath());
      System.out.println("- Image URL path set to: " + imagePath);
      System.out.println("- Used directory: " + usedDirectory);
      System.out.println("- User ID: " + user.getId());
      
      // Create response with updated user data
      Map<String, Object> response = new HashMap<>();
      Map<String, Object> userMap = new HashMap<>();
      userMap.put("id", user.getId());
      userMap.put("name", user.getName());
      userMap.put("email", user.getEmail());
      userMap.put("image", user.getImage());
      userMap.put("role", user.getRole());
      response.put("user", userMap);
      
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      System.err.println("Error uploading profile image: " + e.getMessage());
      e.printStackTrace();
      return ResponseEntity.internalServerError().body("Error uploading profile image: " + e.getMessage());
    }
  }
  
  /**
   * Delete a user's profile image
   * @param userId User ID
   * @return Updated user with null image URL
   */
  @DeleteMapping("/{userId}/profile-image")
  public ResponseEntity<?> deleteProfileImage(@PathVariable Long userId) {
    try {
      // Check if user exists
      Optional<User> userOpt = userRepository.findById(userId);
      if (userOpt.isEmpty()) {
        return ResponseEntity.status(404).body("User not found");
      }

      User user = userOpt.get();
      
      // Verify authorization - only allow users to delete their own profile image
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication == null || !authentication.isAuthenticated() || 
          !authentication.getName().equals(user.getEmail())) {
        return ResponseEntity.status(403).body("Not authorized to update this user's profile image");
      }
      
      // Delete the file if it exists
      String imagePath = user.getImage();
      if (imagePath != null && !imagePath.isEmpty()) {
        // Define possible upload directories
        String[] uploadDirs = {
            System.getProperty("user.dir") + "/uploads/profiles/", // Project directory
            System.getProperty("java.io.tmpdir") + "/uploads/profiles/", // Temp directory
            System.getProperty("user.home") + "/uploads/profiles/", // Home directory
            "uploads/profiles/" // Relative directory
        };
        
        if (imagePath.contains("/")) {
          // Extract the filename from the image path
          String filename = imagePath.substring(imagePath.lastIndexOf("/") + 1);
          
          boolean deleted = false;
          
          // Try to delete from all possible locations
          for (String dir : uploadDirs) {
            File imageFile = new File(dir, filename);
            if (imageFile.exists() && imageFile.isFile()) {
              try {
                boolean success = imageFile.delete();
                if (success) {
                  System.out.println("Successfully deleted file: " + imageFile.getAbsolutePath());
                  deleted = true;
                } else {
                  System.err.println("Failed to delete file: " + imageFile.getAbsolutePath());
                }
              } catch (Exception e) {
                System.err.println("Error deleting file " + imageFile.getAbsolutePath() + ": " + e.getMessage());
              }
            }
          }
          
          if (!deleted) {
            System.err.println("Could not find or delete profile image file for path: " + imagePath);
          }
        }
      }
      
      // Update the user's image field to null
      user.setImage(null);
      userRepository.save(user);
      
      System.out.println("Profile image removed for user ID: " + user.getId());
      
      // Create response with updated user data
      Map<String, Object> response = new HashMap<>();
      Map<String, Object> userMap = new HashMap<>();
      userMap.put("id", user.getId());
      userMap.put("name", user.getName());
      userMap.put("email", user.getEmail());
      userMap.put("image", user.getImage());
      userMap.put("role", user.getRole());
      response.put("user", userMap);
      
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      System.err.println("Error deleting profile image: " + e.getMessage());
      e.printStackTrace();
      return ResponseEntity.internalServerError().body("Error deleting profile image: " + e.getMessage());
    }
  }
}
