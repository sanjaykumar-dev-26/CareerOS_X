package com.careerosx.backend.service;

import com.careerosx.backend.repository.UserRepository;
import com.careerosx.backend.dto.RegisterRequest;
import com.careerosx.backend.repository.RefreshSessionRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.careerosx.backend.dto.AuthResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.careerosx.backend.entity.RefreshSession;
import static org.mockito.Mockito.never;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.argThat;
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


        // Act
        AuthResponse response = authService.register(request);
        // Assert
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        // Verify actual User object passed to repository
        verify(userRepository).save(argThat(user ->
            user.getPasswordHash().equals("hashedPassword")
        ));

        // Assert: Refresh session is saved and inspect its values
        ArgumentCaptor<RefreshSession> captor =
                ArgumentCaptor.forClass(RefreshSession.class);

        verify(refreshSessionRepository).save(captor.capture());

        RefreshSession savedSession = captor.getValue();
        assertEquals("test@gmail.com", savedSession.getUser().getEmail());
        assertEquals(
                "hashed-refresh-token",
                savedSession.getTokenHash()
        );

        assertEquals(
                Instant.parse("2027-01-01T00:00:00Z"),
                savedSession.getExpiresAt()
        );

        assertTrue(!savedSession.isRevoked());
    }
    @Test
    void registerShouldThrowExceptionWhenEmailAlreadyExists() {
    // Arrange
    when(userRepository.existsByEmail("test@gmail.com"))
            .thenReturn(true);
    RegisterRequest request = new RegisterRequest();
    request.setEmail("test@gmail.com");
    request.setPassword("password123");
    // Act + Assert
    assertThrows(RuntimeException.class, () ->
            authService.register(request)
    );

        // Verify: User save nahi hona chahiye
        verify(userRepository, never()).save(any(User.class));
    }
}