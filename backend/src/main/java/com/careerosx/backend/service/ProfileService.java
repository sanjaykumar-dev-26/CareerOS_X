
package com.careerosx.backend.service;

import com.careerosx.backend.dto.ProfileResponse;
import com.careerosx.backend.entity.User;
import com.careerosx.backend.repository.UserRepository;
import com.careerosx.backend.repository.RefreshSessionRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshSessionRepository refreshSessionRepository;

    public ProfileService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RefreshSessionRepository refreshSessionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshSessionRepository = refreshSessionRepository;
    }

    @Transactional
    public void changePassword(
            String email,
            String currentPassword,
            String newPassword) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"));

        if (!passwordEncoder.matches(
                currentPassword, user.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Current password is incorrect");
        }

        if (passwordEncoder.matches(
                newPassword, user.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New password must differ from current password");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.incrementTokenVersion(); // Invalidate existing tokens
        refreshSessionRepository.revokeAllByUserId(user.getId()); // Revoke all refresh sessions
        userRepository.save(user);
    }
}
