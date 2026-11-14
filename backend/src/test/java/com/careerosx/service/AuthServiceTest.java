package com.careerosx.backend.service;

import com.careerosx.backend.repository.UserRepository;
import com.careerosx.backend.dto.RegisterRequest;
import com.careerosx.backend.repository.RefreshSessionRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.when;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.careerosx.backend.entity.User;
import static org.mockito.ArgumentMatchers.any;
import java.time.Instant;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshSessionRepository refreshSessionRepository;

    @InjectMocks
    private AuthService authService;



    @Test
    void registerShouldCreateUserSuccessfully() {

        // Arrange
        when(userRepository.existsByEmail("test@gmail.com"))
        .thenReturn(false);
        when(passwordEncoder.encode("password123"))
        .thenReturn("hashedPassword");
        when(userRepository.save(any(User.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
        RegisterRequest request = new RegisterRequest();
        when(jwtService.generateToken("test@gmail.com", 0))
        .thenReturn("access-token");
        when(jwtService.generateRefreshToken("test@gmail.com", 0))
        .thenReturn("refresh-token");
        when(jwtService.hashToken("refresh-token"))
        .thenReturn("hashed-refresh-token");
        when(jwtService.getRefreshTokenExpiry("refresh-token"))
        .thenReturn(Instant.parse("2027-01-01T00:00:00Z"));
        request.setEmail("test@gmail.com");
        request.setPassword("password123");

    }
}