package com.fuar.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuar.dto.CreateUserRequest;
import com.fuar.dto.UpdateUserRequest;
import com.fuar.model.Role;
import com.fuar.model.User;
import com.fuar.model.UserInfo;
import com.fuar.repository.UserRepository;
import com.fuar.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
    private final EventService eventService;
    private final SkillService skillService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.profile.images.dir:${app.upload.dir}/profiles}")
    private String profileImagesDir;

    @PostConstruct
    public void init() {
        try {
            // Get project root directory
            String projectRoot = System.getProperty("user.dir");
            String projectUploadDir = projectRoot + "/" + uploadDir;
            String projectProfileImagesDir = projectRoot + "/" + profileImagesDir;
            
            // Create all required directories
            Files.createDirectories(Paths.get(projectUploadDir));
            Files.createDirectories(Paths.get(projectProfileImagesDir));
            
            // Also try to create in alternative locations
            String[] alternativeDirs = {
                System.getProperty("java.io.tmpdir") + "/" + uploadDir,
                System.getProperty("user.home") + "/" + uploadDir,
                uploadDir
            };
            
            for (String dir : alternativeDirs) {
                try {
                    Files.createDirectories(Paths.get(dir));
                    Files.createDirectories(Paths.get(dir + "/profiles"));
                    
                    // Set directory permissions on Unix systems
                    try {
                        Process process = Runtime.getRuntime().exec("chmod -R 755 " + dir);
                        int exitCode = process.waitFor();
                        if (exitCode == 0) {
                            System.out.println("Set permissions for directory: " + dir);
                        }
                    } catch (Exception e) {
                        System.out.println("Could not set permissions for " + dir + ": " + e.getMessage());
                    }
                } catch (Exception e) {
                    System.out.println("Could not create directory " + dir + ": " + e.getMessage());
                }
            }
            
        } catch (IOException e) {
            System.err.println("Warning: Could not create all upload directories: " + e.getMessage());
            // Don't throw exception, just log warning
        }
    }

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

  /**
   * Create a new user - Only accessible by admin users
   * @param request User creation request
   * @return Created user with UserInfo and other details
   */
  @PostMapping(consumes = { "multipart/form-data" })
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> createUser(
      @RequestPart("userData") @Valid CreateUserRequest request,
      @RequestPart(value = "file", required = false) MultipartFile file) {
      try {
          // Validate request
          if (request.getEmail() == null || request.getEmail().isEmpty()) {
              return ResponseEntity.badRequest().body("Email is required");
          }
          if (request.getPassword() == null || request.getPassword().isEmpty()) {
              return ResponseEntity.badRequest().body("Password is required");
          }
          if (request.getName() == null || request.getName().isEmpty()) {
              return ResponseEntity.badRequest().body("Name is required");
          }

          // Check for duplicate email
          if (userRepository.findByEmail(request.getEmail()).isPresent()) {
              return ResponseEntity.badRequest().body("Email address is already in use");
          }

          // Handle file upload if present
          String imagePath = null;
          if (file != null && !file.isEmpty()) {
              try {
                  // Get project root directory
                  String projectRoot = System.getProperty("user.dir");
                  String projectUploadDir = projectRoot + "/uploads/profiles";
                  
                  // Define alternative upload directories as fallbacks
                  String[] uploadDirs = {
                      projectUploadDir, // Project directory (primary)
                      System.getProperty("java.io.tmpdir") + "/uploads/profiles", // Temp directory
                      System.getProperty("user.home") + "/uploads/profiles", // Home directory
                      "uploads/profiles" // Relative directory
                  };
                  
                  // Generate unique filename
                  String originalFilename = file.getOriginalFilename();
                  String extension = originalFilename != null && originalFilename.contains(".") ? 
                      originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
                  String filename = "profile_" + System.currentTimeMillis() + extension;
                  
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
                              if (!created) {
                                  System.err.println("Failed to create directory: " + directory.getAbsolutePath());
                                  continue;
                              }
                              
                              // Try to set permissions
                              try {
                                  Process process = Runtime.getRuntime().exec("chmod 755 " + directory.getAbsolutePath());
                                  process.waitFor();
                              } catch (Exception e) {
                                  System.out.println("Could not set permissions: " + e.getMessage());
                              }
                          } catch (Exception e) {
                              System.err.println("Error creating directory " + directory.getAbsolutePath() + ": " + e.getMessage());
                              continue;
                          }
                      }
                      
                      if (!directory.canWrite()) {
                          System.err.println("Directory not writable: " + directory.getAbsolutePath());
                          continue;
                      }
                      
                      File targetFile = new File(directory, filename);
                      try {
                          file.transferTo(targetFile);
                          if (targetFile.exists() && targetFile.length() > 0) {
                              savedFile = targetFile;
                              usedDirectory = dir;
                              break;
                          }
                      } catch (Exception e) {
                          lastException = e;
                          System.err.println("Error saving to " + targetFile.getAbsolutePath() + ": " + e.getMessage());
                      }
                  }
                  
                  if (savedFile == null) {
                      throw new IOException("Failed to save file to any location" + 
                          (lastException != null ? ": " + lastException.getMessage() : ""));
                  }
                  
                  // Store relative path for URL construction
                  imagePath = "/uploads/profiles/" + filename;
              } catch (IOException e) {
                  return ResponseEntity.internalServerError()
                      .body("Error uploading profile image: " + e.getMessage());
              }
          }

          // Create and save User entity with image path
          User user = User.builder()
              .name(request.getName())
              .email(request.getEmail())
              .password(passwordEncoder.encode(request.getPassword()))
              .role(request.getRole() != null ? request.getRole() : Role.USER)
              .image(imagePath)
              .build();

          User savedUser = userRepository.save(user);

          // Create and link UserInfo
          try {
              boolean created = userInfoService.createUserInfoIfNotExists(savedUser.getId());
              if (!created) {
                  throw new RuntimeException("Failed to create UserInfo for user");
              }

              // Update UserInfo if additional details were provided
              if (request.getUserInfo() != null) {
                  UserInfo updatedInfo = userInfoService.updateUserInfoFields(savedUser.getId(), request.getUserInfo());
                  if (updatedInfo != null) {
                      savedUser = userRepository.findById(savedUser.getId()).orElse(savedUser);
                  }
              }

              // Remove sensitive information before returning
              savedUser.setPassword(null);

              // Create response with basic user info and UserInfo details
              Map<String, Object> response = new HashMap<>();
              response.put("id", savedUser.getId());
              response.put("name", savedUser.getName());
              response.put("email", savedUser.getEmail());
              response.put("role", savedUser.getRole());
              response.put("image", savedUser.getImage());
              response.put("userInfo", savedUser.getUserInfo());

              return ResponseEntity.ok(response);

          } catch (Exception e) {
              // If UserInfo creation fails, delete the user and return error
              userRepository.delete(savedUser);
              throw new RuntimeException("Failed to create user profile: " + e.getMessage());
          }
      } catch (Exception e) {
          return ResponseEntity.internalServerError()
              .body("Error creating user: " + e.getMessage());
      }
  }

  /**
   * Delete a user and all associated data
   * Only accessible by admin users
   * @param userId ID of the user to delete
   * @return Empty response if successful
   */
  @DeleteMapping("/{userId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
      try {
          // First check if user exists
          User user = userRepository.findById(userId)
                  .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

          // Get user info (if exists)
          Optional<UserInfo> userInfoOpt = Optional.empty();
          try {
              userInfoOpt = Optional.ofNullable(userInfoService.getUserInfo(userId));
          } catch (RuntimeException e) {
              // UserInfo not found, continue with deletion
              System.out.println("No UserInfo found for user " + userId);
          }

          if (userInfoOpt.isPresent()) {
              UserInfo userInfo = userInfoOpt.get();
              Long userInfoId = userInfo.getId();

              // Delete all education records
              user.getUserInfo().getEducations().forEach(education -> 
                  educationService.deleteEducation(education.getId()));

              // Delete all work experience records
              user.getUserInfo().getWorkExperiences().forEach(workExperience -> 
                  workExperienceService.deleteWorkExperience(workExperience.getId()));

              // Clear skills (but don't delete the skills themselves)
              user.getUserInfo().getSkills().clear();
              userRepository.save(user);
          }

          // Clean up event relationships
          try {
              // Remove user from all events where they are a speaker
              eventService.removeUserFromAllEvents(userId);
              
              // Remove user from all events where they are an attendee
              eventService.unregisterFromAllEvents(userId);
          } catch (Exception e) {
              System.err.println("Error cleaning up event relationships: " + e.getMessage());
              // Continue with deletion even if event cleanup fails
          }

          // Delete user (this will cascade delete UserInfo and related records)
          userRepository.delete(user);

          // Clean up profile image if exists
          String imagePath = user.getImage();
          if (imagePath != null && !imagePath.isEmpty()) {
              // Define possible upload directories
              String[] uploadDirs = {
                  System.getProperty("user.dir") + "/uploads/profiles/",
                  System.getProperty("java.io.tmpdir") + "/uploads/profiles/",
                  System.getProperty("user.home") + "/uploads/profiles/",
                  "uploads/profiles/"
              };

              if (imagePath.contains("/")) {
                  String filename = imagePath.substring(imagePath.lastIndexOf("/") + 1);
                  for (String dir : uploadDirs) {
                      File imageFile = new File(dir, filename);
                      if (imageFile.exists() && imageFile.isFile()) {
                          boolean deleted = imageFile.delete();
                          if (deleted) {
                              System.out.println("Successfully deleted profile image: " + imageFile.getAbsolutePath());
                          } else {
                              System.err.println("Failed to delete profile image: " + imageFile.getAbsolutePath());
                          }
                      }
                  }
              }
          }

          return ResponseEntity.ok().build();
      } catch (Exception e) {
          System.err.println("Error deleting user: " + e.getMessage());
          e.printStackTrace();
          return ResponseEntity.internalServerError()
                  .body("Error deleting user: " + e.getMessage());
      }
  }

  /**
   * Update a user and their related information with file upload support
   * @param userId User ID to update
   * @param userData User data as JSON string
   * @param file Optional profile image file
   * @return Updated user
   */
  @PutMapping(value = "/{userId}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
  @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
  public ResponseEntity<?> updateUserWithImage(
          @PathVariable Long userId,
          @RequestPart(value = "userData", required = true) String userDataJson,
          @RequestPart(value = "file", required = false) MultipartFile file) {
      try {
          // Parse user data from JSON
          ObjectMapper objectMapper = new ObjectMapper();
          // Configure Jackson to ignore unknown properties
          objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
          UpdateUserRequest updateUserRequest = objectMapper.readValue(userDataJson, UpdateUserRequest.class);
          
          // Check if user exists
          User user = userRepository.findById(userId)
                  .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

          // Process and save the image if provided
          if (file != null && !file.isEmpty()) {
              try {
                  String fileName = StringUtils.cleanPath(file.getOriginalFilename());
                  String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
                  
                  // Save the file
                  Path uploadPath = Paths.get(System.getProperty("user.dir"), profileImagesDir);
                  if (!Files.exists(uploadPath)) {
                      Files.createDirectories(uploadPath);
                  }
                  
                  Path filePath = uploadPath.resolve(uniqueFileName);
                  Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                  
                  // Update the user's image path
                  user.setImage(uniqueFileName);
                  System.out.println("Updated user image: " + uniqueFileName);
              } catch (IOException e) {
                  e.printStackTrace();
                  return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                          .body("Could not upload the image: " + e.getMessage());
              }
          }

          // Update basic user information
          if (updateUserRequest.getName() != null) {
              user.setName(updateUserRequest.getName());
          }
          
          // Only admins can change roles
          Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
          boolean isAdmin = authentication.getAuthorities().stream()
                  .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
          
          if (isAdmin && updateUserRequest.getRole() != null) {
              user.setRole(updateUserRequest.getRole());
          }
          
          // Update email if provided and different from current
          if (updateUserRequest.getEmail() != null && !updateUserRequest.getEmail().equals(user.getEmail())) {
              // Check if email is already in use
              if (userRepository.findByEmail(updateUserRequest.getEmail()).isPresent()) {
                  return ResponseEntity.badRequest().body("Email is already in use");
              }
              user.setEmail(updateUserRequest.getEmail());
          }
          
          // Update password if provided
          if (updateUserRequest.getPassword() != null && !updateUserRequest.getPassword().isEmpty()) {
              user.setPassword(passwordEncoder.encode(updateUserRequest.getPassword()));
          }
          
          // Save updated user
          User updatedUser = userRepository.save(user);
          
          // Update UserInfo if provided
          if (updateUserRequest.getUserInfo() != null) {
              try {
                  // Try to get or create UserInfo
                  userInfoService.createUserInfoIfNotExists(userId);
                  
                  // Update UserInfo fields
                  userInfoService.updateUserInfoFields(userId, updateUserRequest.getUserInfo());
              } catch (Exception e) {
                  System.err.println("Error updating UserInfo: " + e.getMessage());
                  e.printStackTrace();
                  // Continue even if UserInfo update fails
              }
          }
          
          // Get the updated user with refreshed relations
          User refreshedUser = userRepository.findById(userId).orElse(updatedUser);
          refreshedUser.setPassword(null); // Don't return password in response
          
          return ResponseEntity.ok(refreshedUser);
      } catch (Exception e) {
          System.err.println("Error updating user with image: " + e.getMessage());
          e.printStackTrace();
          return ResponseEntity.internalServerError().body("Error updating user: " + e.getMessage());
      }
  }
  
  // Keep the original JSON-based update method for backward compatibility
  @PutMapping(value = "/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
  public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody @Valid UpdateUserRequest updateUserRequest) {
      try {
          // Check if user exists
          User user = userRepository.findById(userId)
                  .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

          // Update basic user information
          if (updateUserRequest.getName() != null) {
              user.setName(updateUserRequest.getName());
          }
          
          // Only admins can change roles
          Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
          boolean isAdmin = authentication.getAuthorities().stream()
                  .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
          
          if (isAdmin && updateUserRequest.getRole() != null) {
              user.setRole(updateUserRequest.getRole());
          }
          
          // Update email if provided and different from current
          if (updateUserRequest.getEmail() != null && !updateUserRequest.getEmail().equals(user.getEmail())) {
              // Check if email is already in use
              if (userRepository.findByEmail(updateUserRequest.getEmail()).isPresent()) {
                  return ResponseEntity.badRequest().body("Email is already in use");
              }
              user.setEmail(updateUserRequest.getEmail());
          }
          
          // Update password if provided
          if (updateUserRequest.getPassword() != null && !updateUserRequest.getPassword().isEmpty()) {
              user.setPassword(passwordEncoder.encode(updateUserRequest.getPassword()));
          }
          
          // Update image if provided
          if (updateUserRequest.getImage() != null) {
              user.setImage(updateUserRequest.getImage());
          }
          
          // Save updated user
          User updatedUser = userRepository.save(user);
          
          // Update UserInfo if provided
          if (updateUserRequest.getUserInfo() != null) {
              try {
                  // Try to get or create UserInfo
                  userInfoService.createUserInfoIfNotExists(userId);
                  
                  // Update UserInfo fields
                  userInfoService.updateUserInfoFields(userId, updateUserRequest.getUserInfo());
              } catch (Exception e) {
                  System.err.println("Error updating UserInfo: " + e.getMessage());
                  e.printStackTrace();
                  // Continue even if UserInfo update fails
              }
          }
          
          // Get the updated user with refreshed relations
          User refreshedUser = userRepository.findById(userId).orElse(updatedUser);
          refreshedUser.setPassword(null); // Don't return password in response
          
          return ResponseEntity.ok(refreshedUser);
      } catch (Exception e) {
          System.err.println("Error updating user: " + e.getMessage());
          e.printStackTrace();
          return ResponseEntity.internalServerError().body("Error updating user: " + e.getMessage());
      }
  }
}
