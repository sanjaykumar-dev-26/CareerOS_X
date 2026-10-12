package com.careerosx.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import com.careerosx.backend.entity.User;
import com.careerosx.backend.dto.ProfileResponse;
import org.springframework.security.core.Authentication;
import com.careerosx.backend.repository.UserRepository;

@RestController
@RequestMapping("/test")
public class TestController {
    
    private UserRepository userRepository;
    public TestController(UserRepository userRepository) {
    this.userRepository = userRepository;
    }
    @GetMapping("/protected")
    public String protectedEndpoint(Authentication authentication) {
        return  authentication.getName();
    }
}




