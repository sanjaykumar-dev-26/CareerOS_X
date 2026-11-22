package com.careerosx.backend.service;

import com.careerosx.backend.entity.User;
import com.careerosx.backend.repository.UserRepository;
import com.careerosx.backend.repository.RefreshSessionRepository;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshSessionRepository refreshSessionRepository;

    @InjectMocks
    private ProfileService profileService;

    @Test
void changePasswordShouldUpdatePasswordSuccessfully() {

    // Arrange
    String email = "test@example.com";
    String currentPassword = "oldPassword123";
    String newPassword = "newPassword123";

    User user = new User(email, "oldHashedPassword");

    when(userRepository.findByEmail(email))
            .thenReturn(Optional.of(user));

    when(passwordEncoder.matches(currentPassword, "oldHashedPassword"))
            .thenReturn(true);

    when(passwordEncoder.matches(newPassword, "oldHashedPassword"))
            .thenReturn(false);

    when(passwordEncoder.encode(newPassword))
            .thenReturn("newHashedPassword");

    // Act
    profileService.changePassword(email, currentPassword, newPassword);

    // Assert
    assertEquals("newHashedPassword", user.getPasswordHash());
    assertEquals(1, user.getTokenVersion());
    verify(refreshSessionRepository).revokeAllByUserId(user.getId());

    verify(userRepository).save(user);
}
@Test
void changePasswordShouldThrowExceptionWhenCurrentPasswordIsIncorrect() {

    // Arrange
    String email = "test@example.com";
    String currentPassword = "wrongPassword";
    String newPassword = "newPassword123";

    User user = new User(email, "oldHashedPassword");

    when(userRepository.findByEmail(email))
            .thenReturn(Optional.of(user));

    when(passwordEncoder.matches(currentPassword, "oldHashedPassword"))
            .thenReturn(false);
    // Act + Assert
    ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> profileService.changePassword(
                    email, currentPassword, newPassword));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

    // Verify
    verify(passwordEncoder, never()).encode(anyString());

    verify(refreshSessionRepository, never())
            .revokeAllByUserId(anyLong());

    verify(userRepository, never()).save(any(User.class));
}
@Test
void changePasswordShouldThrowExceptionWhenNewPasswordIsSameAsCurrentPassword() {

    // Arrange
    String email = "test@example.com";
    String currentPassword = "oldPassword123";
    String newPassword = "oldPassword123";

    User user = new User(email, "oldHashedPassword");

    when(userRepository.findByEmail(email))
            .thenReturn(Optional.of(user));

    when(passwordEncoder.matches(anyString(), eq("oldHashedPassword")))
            .thenReturn(true);

    // Act + Assert
    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> profileService.changePassword(
                email, currentPassword, newPassword));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    // Verify
    verify(passwordEncoder, never()).encode(anyString());

    verify(refreshSessionRepository, never())
            .revokeAllByUserId(anyLong());

    verify(userRepository, never()).save(any(User.class));
}
@Test
void changePasswordShouldThrowExceptionWhenUserDoesNotExist() {

    // Arrange
    String email = "unknown@example.com";

    when(userRepository.findByEmail(email))
            .thenReturn(Optional.empty());

    // Act + Assert
    ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> profileService.changePassword(
                    email, "oldPassword123", "newPassword123")
    );

    // Assert status
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

    // Verify
    verify(passwordEncoder, never()).matches(anyString(), anyString());

    verify(passwordEncoder, never()).encode(anyString());

    verify(refreshSessionRepository, never())
            .revokeAllByUserId(anyLong());

    verify(userRepository, never()).save(any(User.class));
}
}