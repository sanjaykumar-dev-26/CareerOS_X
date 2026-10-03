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

        String accessToken = jwtService.generateToken(savedUser.getEmail());
        String refreshToken = jwtService.generateRefreshToken(savedUser.getEmail());
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
                String accessToken = jwtService.generateToken(user.getEmail());
                String refreshToken = jwtService.generateRefreshToken(user.getEmail());
                return new AuthResponse(accessToken, refreshToken);
            }            
        }
    }   
