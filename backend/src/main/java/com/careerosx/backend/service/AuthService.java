package com.careerosx.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.careerosx.backend.entity.RefreshSession;
import com.careerosx.backend.repository.RefreshSessionRepository;
import com.careerosx.backend.entity.User;
import com.careerosx.backend.dto.AuthResponse;
import com.careerosx.backend.dto.LoginRequest;
import com.careerosx.backend.dto.RegisterRequest;
import com.careerosx.backend.repository.UserRepository;
import java.time.Instant;
import org.springframework.transaction.annotation.Transactional;
@Service 
public class AuthService {
        private UserRepository userRepository;
        private PasswordEncoder passwordEncoder;
        private JwtService jwtService;
        private RefreshSessionRepository refreshSessionRepository;

        public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, RefreshSessionRepository refreshSessionRepository) {
            this.userRepository = userRepository;
            this.passwordEncoder = passwordEncoder;
            this.jwtService = jwtService;
            this.refreshSessionRepository = refreshSessionRepository;
        }
        public AuthResponse register(RegisterRequest registerRequest) {  
        boolean present=userRepository.existsByEmail(registerRequest.getEmail());
        if(present){
            throw new RuntimeException("user already exists");
        }else{
            String hashedPassword=passwordEncoder.encode(registerRequest.getPassword());
            User user=new User(registerRequest.getEmail(),hashedPassword);
            User savedUser=userRepository.save(user);

        String accessToken = jwtService.generateToken(
        savedUser.getEmail(), savedUser.getTokenVersion());

        String refreshToken = jwtService.generateRefreshToken(
                    savedUser.getEmail(), savedUser.getTokenVersion());

        saveRefreshSession(savedUser, refreshToken);

        return new AuthResponse(accessToken, refreshToken);
        }
    }
    private void saveRefreshSession(User user, String refreshToken) {
    String tokenHash = jwtService.hashToken(refreshToken);
    Instant expiresAt = jwtService.getRefreshTokenExpiry(refreshToken);

    RefreshSession session =
            new RefreshSession(user, tokenHash, expiresAt);

    refreshSessionRepository.save(session);
    }
    public AuthResponse login(LoginRequest loginRequest) {

            User user=userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(()->new RuntimeException("user not found"));
            boolean passwordMatch=passwordEncoder.matches(loginRequest.getPassword(),user.getPasswordHash());
            if(!passwordMatch){ 
                throw new RuntimeException("invalid password");
            }           
            else{
            String accessToken = jwtService.generateToken(
                    user.getEmail(), user.getTokenVersion());

            String refreshToken = jwtService.generateRefreshToken(
                    user.getEmail(), user.getTokenVersion());

            saveRefreshSession(user, refreshToken);

            return new AuthResponse(accessToken, refreshToken);
            }            
        }
        @Transactional
        public AuthResponse refreshAccessToken(String refreshToken) {

        // 1. Validate signature and expiry
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }
        // 2. Ensure this is a refresh token, not an access token
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new RuntimeException("Token is not a refresh token");
        }
        // 3. Find the refresh session in database
        String tokenHash = jwtService.hashToken(refreshToken);

        RefreshSession session = refreshSessionRepository
        .findByTokenHashForUpdate(tokenHash)
        .orElseThrow(() ->
                new RuntimeException("Refresh token not found or already used")
        );  
        // 4. Check whether session is revoked
        if (session.isRevoked()) {
            throw new RuntimeException("Refresh session has been revoked");
        }

        // 5. Check whether session has expired
        if (session.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh session has expired");
        }
        // 6. Extract user details from token
        String email = jwtService.extractEmail(refreshToken);
        int tokenVersion = jwtService.extractTokenVersion(refreshToken);
        // 7.Verify that the session belongs to the token's user
        if (!session.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Refresh session user mismatch");
        }
        // 8. Find user and verify current token version
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getTokenVersion() != tokenVersion) {
            throw new RuntimeException("Refresh token has been invalidated");
        }
        // 9. Generate a new access token
        String newAccessToken = jwtService.generateToken(user.getEmail(), user.getTokenVersion());
        // 10. Return the new access token and a new refresh token
        String newRefreshToken = jwtService.generateRefreshToken(user.getEmail(), user.getTokenVersion());
        // Generate hash of the new refresh token
        String newTokenHash = jwtService.hashToken(newRefreshToken);
        // Get expiry of the new refresh token
        Instant newExpiry = jwtService.getRefreshTokenExpiry(newRefreshToken);
        // Update the existing session
        session.setTokenHash(newTokenHash);
        session.setExpiresAt(newExpiry);
        refreshSessionRepository.save(session);
        return new AuthResponse(newAccessToken, newRefreshToken);
    }
    @Transactional
    public void logout(String refreshToken) {
    if (!jwtService.isTokenValid(refreshToken)
            || !jwtService.isRefreshToken(refreshToken)) {
        throw new RuntimeException("Invalid refresh token");
    }
    String tokenHash = jwtService.hashToken(refreshToken);
    RefreshSession session = refreshSessionRepository
            .findByTokenHashForUpdate(tokenHash)
            .orElseThrow(() ->
                    new RuntimeException("Refresh session not found")
            );

    session.setRevoked(true);
}

}

