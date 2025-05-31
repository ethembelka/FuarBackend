package com.fuar.controller;

import com.fuar.model.Role;
import com.fuar.model.User;
import com.fuar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;

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
}
