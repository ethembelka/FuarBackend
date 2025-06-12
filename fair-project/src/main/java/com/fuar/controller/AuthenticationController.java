package com.fuar.controller;

import com.fuar.dto.AuthenticationRequest;
import com.fuar.dto.AuthenticationResponse;
import com.fuar.dto.CurrentUserResponse;
import com.fuar.dto.RegisterRequest;
import com.fuar.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import com.fuar.service.AuthenticationService;
import com.fuar.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @GetMapping("/current-user")
    public ResponseEntity<CurrentUserResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        try {
            String email = authentication.getName();
            System.out.println("Getting current user for email: " + email); // Debug log
            
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        System.err.println("User not found for email: " + email); // Debug log
                        return new UsernameNotFoundException("User not found for email: " + email);
                    });
            
            System.out.println("Found user: " + user.getName() + ", Role: " + user.getRole()); // Debug log
            
            return ResponseEntity.ok(CurrentUserResponse.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .image(user.getImage())
                    .build());
        } catch (Exception e) {
            System.err.println("Error in getCurrentUser: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
