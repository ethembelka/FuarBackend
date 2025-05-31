package com.fuar.service;

import com.fuar.dto.AuthenticationRequest;
import com.fuar.dto.AuthenticationResponse;
import com.fuar.dto.RegisterRequest;
import com.fuar.model.Role;
import com.fuar.model.Token;
import com.fuar.model.TokenType;
import com.fuar.model.User;
import com.fuar.repository.TokenRepository;
import com.fuar.repository.UserRepository;
import com.fuar.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new com.fuar.exception.DuplicateResourceException("Bu e-posta adresi ile kayıtlı bir kullanıcı zaten mevcut");
        }
        
        try {
            var user = User.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(Role.USER)
                    .build();
            var savedUser = userRepository.save(user);
            var jwtToken = jwtService.generateToken(user);
            var refreshToken = jwtService.generateRefreshToken(user);
            saveUserToken(savedUser, jwtToken);
            return AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .build();
        } catch (Exception e) {
            // Log the error
            System.err.println("Error during user registration: " + e.getMessage());
            // Rethrow as a more user-friendly exception
            throw new com.fuar.exception.BadRequestException("Kullanıcı kaydı sırasında bir hata oluştu. Lütfen daha sonra tekrar deneyiniz.");
        }
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            throw new com.fuar.exception.AuthenticationException("E-posta veya şifre hatalı");
        } catch (org.springframework.security.authentication.DisabledException e) {
            throw new com.fuar.exception.AuthenticationException("Hesabınız devre dışı bırakılmıştır");
        } catch (org.springframework.security.authentication.LockedException e) {
            throw new com.fuar.exception.AuthenticationException("Hesabınız kilitlenmiştir");
        }

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new com.fuar.exception.AuthenticationException("Kullanıcı bulunamadı"));
                
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokensByUser(user.getId());
        if (validUserTokens.isEmpty()) {
            return;
        }
        validUserTokens.forEach(t -> {
            t.setExpired(true);
            t.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }
}
