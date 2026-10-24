package com.careerosx.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.careerosx.backend.entity.User;
import com.careerosx.backend.dto.AuthResponse;
import com.careerosx.backend.dto.LoginRequest;
import com.careerosx.backend.dto.RegisterRequest;
import com.careerosx.backend.repository.UserRepository;

@Service 
public class AuthService {
        private UserRepository userRepository;
        private PasswordEncoder passwordEncoder;
        private JwtService jwtService;

        public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
            this.userRepository = userRepository;
            this.passwordEncoder = passwordEncoder;
            this.jwtService = jwtService;
        }
        public AuthResponse register(RegisterRequest registerRequest) {  
        boolean present=userRepository.existsByEmail(registerRequest.getEmail());
        if(present){
            throw new RuntimeException("user already exists");
        }else{
            String hashedPassword=passwordEncoder.encode(registerRequest.getPassword());
            User user=new User(registerRequest.getEmail(),hashedPassword);
            User savedUser=userRepository.save(user);

        String accessToken = jwtService.generateToken(savedUser.getEmail(),savedUser.getTokenVersion());
        String refreshToken = jwtService.generateRefreshToken(savedUser.getEmail(),savedUser.getTokenVersion());
        return new AuthResponse(accessToken, refreshToken);
        }
    }
        
        public AuthResponse login(LoginRequest loginRequest) {

            User user=userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(()->new RuntimeException("user not found"));
            boolean passwordMatch=passwordEncoder.matches(loginRequest.getPassword(),user.getPasswordHash());
            if(!passwordMatch){ 
                throw new RuntimeException("invalid password");
            }           
            else{
                String accessToken = jwtService.generateToken(user.getEmail(),user.getTokenVersion());
                String refreshToken = jwtService.generateRefreshToken(user.getEmail(),user.getTokenVersion());
                return new AuthResponse(accessToken, refreshToken);
            }            
        }
        public String refreshAccessToken(String refreshToken) {

        // 1. Validate signature and expiry
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }
        // 2. Ensure this is a refresh token, not an access token
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new RuntimeException("Token is not a refresh token");
        }
        // 3. Extract user details from token
        String email = jwtService.extractEmail(refreshToken);
        int tokenVersion = jwtService.extractTokenVersion(refreshToken);
        // 4. Find user and verify current token version
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getTokenVersion() != tokenVersion) {
            throw new RuntimeException("Refresh token has been invalidated");
        }
        // 5. Generate a new access token
        return jwtService.generateToken(user.getEmail(), user.getTokenVersion());
    }
}   
