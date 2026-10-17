
package com.careerosx.backend.controller;

import com.careerosx.backend.dto.ProfileResponse;
import com.careerosx.backend.entity.User;
import com.careerosx.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import com.careerosx.backend.dto.UpdateProfileRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.careerosx.backend.dto.ChangePasswordRequest;
import com.careerosx.backend.service.ProfileService;
import org.springframework.http.ResponseEntity;


@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final ProfileService profileService;

    public ProfileController(UserRepository userRepository,ProfileService profileService) {
        this.userRepository = userRepository;
        this.profileService = profileService;
    }

    @GetMapping
    public ProfileResponse getProfile(Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User not found"
        ));

        return new ProfileResponse(user.getId(), user.getEmail());
    }
    @PutMapping
    public ProfileResponse updateProfile(@Valid @RequestBody UpdateProfileRequest request, Authentication authentication){
        
        String currentEmail = authentication.getName();
        String newEmail = request.getEmail();
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResponseStatusException(
        HttpStatus.NOT_FOUND,
        "User not found"
   ));
        if (!newEmail.equalsIgnoreCase(currentEmail)&& userRepository.existsByEmail(newEmail)) {
        throw new ResponseStatusException( HttpStatus.CONFLICT,"Email is already registered");
        }
        user.setEmail(request.getEmail());
        User updatedUser = userRepository.save(user);
        return new ProfileResponse(updatedUser.getId(), updatedUser.getEmail());
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,Authentication authentication) {
    profileService.changePassword(
            authentication.getName(),
            request.getCurrentPassword(),
            request.getNewPassword()
    );
    return ResponseEntity.noContent().build();
    }   
}
