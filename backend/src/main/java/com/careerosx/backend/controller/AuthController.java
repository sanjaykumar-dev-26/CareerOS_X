package com.careerosx.backend.controller;

import org.springframework.web.bind.annotation.PostMapping;
import com.careerosx.backend.dto.AuthResponse;
import com.careerosx.backend.dto.RegisterRequest;
import com.careerosx.backend.dto.LoginRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.careerosx.backend.service.AuthService;

@RestController 
@RequestMapping ("/auth")
public class AuthController {
    private AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping ("/register")
    public AuthResponse register(@RequestBody RegisterRequest registerRequest) {
        return authService.register(registerRequest);
    }
    @PostMapping ("/login")
    public AuthResponse login(@RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }
}
