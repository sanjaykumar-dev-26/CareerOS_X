package com.careerosx.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/test")
public class TestController {
    

    @GetMapping("/protected")
    public String protectedEndpoint(Authentication authentication) {
        return  authentication.getName();
    }
}




