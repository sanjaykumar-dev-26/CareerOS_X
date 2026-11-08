package com.careerosx.backend.controller;

import org.springframework.web.bind.annotation.PostMapping;
import com.careerosx.backend.dto.AuthResponse;
import com.careerosx.backend.dto.RegisterRequest;
import com.careerosx.backend.dto.LoginRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import com.careerosx.backend.dto.RefreshTokenRequest;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;

import com.careerosx.backend.service.AuthService;

@RestController 
@RequestMapping ("/auth")
public class AuthController {
    private AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping ("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest registerRequest) {
        return authService.register(registerRequest);
    }
    @PostMapping ("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }
    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refreshAccessToken(request.getRefreshToken());
    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}
