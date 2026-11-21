
package com.careerosx.backend.service;

import com.careerosx.backend.repository.UserRepository;
import com.careerosx.backend.repository.RefreshSessionRepository;
import com.careerosx.backend.dto.RegisterRequest;
import com.careerosx.backend.dto.LoginRequest;
import com.careerosx.backend.dto.AuthResponse;
import com.careerosx.backend.entity.RefreshSession;
import com.careerosx.backend.entity.User;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyInt;

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


    // 1. Registration success test
    @Test
    void registerShouldCreateUserSuccessfully() {

        // Arrange
        when(userRepository.existsByEmail("test@gmail.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(jwtService.generateToken("test@gmail.com", 0))
                .thenReturn("access-token");

        when(jwtService.generateRefreshToken("test@gmail.com", 0))
                .thenReturn("refresh-token");

        when(jwtService.hashToken("refresh-token"))
                .thenReturn("hashed-refresh-token");

        when(jwtService.getRefreshTokenExpiry("refresh-token"))
                .thenReturn(Instant.parse("2027-01-01T00:00:00Z"));

        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("password123");


        // Act
        AuthResponse response = authService.register(request);


        // Assert
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        verify(userRepository).save(argThat(user ->
                user.getPasswordHash().equals("hashedPassword")
        ));

        ArgumentCaptor<RefreshSession> captor =
                ArgumentCaptor.forClass(RefreshSession.class);

        verify(refreshSessionRepository).save(captor.capture());

        RefreshSession savedSession = captor.getValue();

        assertEquals("test@gmail.com", savedSession.getUser().getEmail());
        assertEquals("hashed-refresh-token", savedSession.getTokenHash());

        assertEquals(
                Instant.parse("2027-01-01T00:00:00Z"),
                savedSession.getExpiresAt()
        );

        assertTrue(!savedSession.isRevoked());
    }


    // 2. Registration duplicate email test
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


        // Verify
        verify(userRepository, never()).save(any(User.class));
    }


    // 3. Login success test
    @Test
    void loginShouldReturnTokensWhenCredentialsAreValid() {

        // Arrange
        User user = new User("test@gmail.com", "hashedPassword");

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password123", "hashedPassword"))
                .thenReturn(true);

        when(jwtService.generateToken("test@gmail.com", 0))
                .thenReturn("access-token");

        when(jwtService.generateRefreshToken("test@gmail.com", 0))
                .thenReturn("refresh-token");

        when(jwtService.hashToken("refresh-token"))
                .thenReturn("hashed-refresh-token");

        when(jwtService.getRefreshTokenExpiry("refresh-token"))
                .thenReturn(Instant.parse("2027-01-01T00:00:00Z"));


        // Act
        LoginRequest request = new LoginRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("password123");

        AuthResponse response = authService.login(request);


        // Assert
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        verify(passwordEncoder).matches("password123", "hashedPassword");

        ArgumentCaptor<RefreshSession> captor =
        ArgumentCaptor.forClass(RefreshSession.class);

        verify(refreshSessionRepository).save(captor.capture());

        RefreshSession savedSession = captor.getValue();
        assertEquals("test@gmail.com", savedSession.getUser().getEmail());

        assertEquals("hashed-refresh-token",savedSession.getTokenHash());

        assertEquals(Instant.parse("2027-01-01T00:00:00Z"),savedSession.getExpiresAt());

        assertTrue(!savedSession.isRevoked());
    }

    @Test
    void loginShouldThrowExceptionWhenPasswordIsInvalid() {

    // Arrange
    User user = new User("test@gmail.com", "hashedPassword");

    when(userRepository.findByEmail("test@gmail.com"))
            .thenReturn(Optional.of(user));

    when(passwordEncoder.matches("wrongPassword", "hashedPassword"))
            .thenReturn(false);
    // Act + Assert
    LoginRequest request = new LoginRequest();
    request.setEmail("test@gmail.com");
    request.setPassword("wrongPassword");

    assertThrows(RuntimeException.class, () ->
            authService.login(request)
    );
    // Verify
    verify(jwtService, never()).generateToken(any(), anyInt());
    verify(jwtService, never()).generateRefreshToken(any(), anyInt());
    verify(refreshSessionRepository, never()).save(any(RefreshSession.class));
}
@Test
void loginShouldThrowExceptionWhenUserDoesNotExist() {

    // Arrange
    when(userRepository.findByEmail("unknown@gmail.com"))
            .thenReturn(Optional.empty());

    // Act + Assert
    LoginRequest request = new LoginRequest();
    request.setEmail("unknown@gmail.com");
    request.setPassword("password123");

    assertThrows(RuntimeException.class, () ->
            authService.login(request)
    );

    // Verify
    verify(passwordEncoder, never()).matches(any(), any());

    verify(jwtService, never()).generateToken(any(), anyInt());

    verify(jwtService, never()).generateRefreshToken(any(), anyInt());

    verify(refreshSessionRepository, never()).save(any(RefreshSession.class));
}
        @Test
        void logoutShouldRevokeRefreshSession() {

        // Arrange
        String refreshToken = "refresh-token";

        RefreshSession session = mock(RefreshSession.class);
        when(jwtService.isTokenValid(refreshToken))
        .thenReturn(true);

        when(jwtService.isRefreshToken(refreshToken))
                .thenReturn(true);

        when(jwtService.hashToken(refreshToken))
                .thenReturn("hashed-refresh-token");

        when(refreshSessionRepository.findByTokenHashForUpdate("hashed-refresh-token"))
                .thenReturn(Optional.of(session));

        // Act
        authService.logout(refreshToken);              

        // Assert
        verify(session).setRevoked(true);
        }
}
