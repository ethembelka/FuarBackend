package com.fuar.security;

import com.fuar.repository.TokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        // Skip JWT validation for auth endpoints
        String requestPath = request.getServletPath();
        if (requestPath.startsWith("/api/v1/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;
        System.out.println("JWT Filter - Request URL: " + request.getRequestURL()); // Debug log
        System.out.println("JWT Filter - Auth header: " + (authHeader != null ? "Bearer ***" : "null")); // Debug log
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("JWT Filter - No valid auth header, continuing without authentication"); // Debug log
            filterChain.doFilter(request, response);
            return;
        }
        jwt = authHeader.substring(7);
        System.out.println("JWT Filter - Token length: " + jwt.length()); // Debug log
        System.out.println("JWT Filter - Token starts with: " + jwt.substring(0, Math.min(20, jwt.length()))); // Debug log
        
        try {
            userEmail = jwtService.extractUsername(jwt);
            System.out.println("JWT Filter - Extracted email: " + userEmail); // Debug log
            
            // Additional debugging to check JWT structure
            try {
                String payload = new String(java.util.Base64.getDecoder().decode(jwt.split("\\.")[1]));
                System.out.println("JWT Filter - Token payload: " + payload); // Debug log
            } catch (Exception e) {
                System.out.println("JWT Filter - Could not decode payload: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("JWT Filter - Error extracting email from token: " + e.getMessage());
            e.printStackTrace();
            filterChain.doFilter(request, response);
            return;
        }

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                System.out.println("JWT Filter - Loaded UserDetails for: " + userEmail); // Debug log

                var isTokenValid = tokenRepository.findByToken(jwt)
                        .map(t -> !t.isExpired() && !t.isRevoked())
                        .orElse(false);
                System.out.println("JWT Filter - Token valid: " + isTokenValid); // Debug log

                if (jwtService.isTokenValid(jwt, userDetails) && isTokenValid) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                System.err.println("JWT Filter - Error processing token: " + e.getMessage());
                e.printStackTrace();
            }
        }
        filterChain.doFilter(request, response);
    }
}
