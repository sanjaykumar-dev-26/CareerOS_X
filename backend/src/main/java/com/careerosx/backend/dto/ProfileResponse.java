package com.careerosx.backend.dto;

public class ProfileResponse {
    private Long id;
    private String email;

    public ProfileResponse(Long id, String email) {
        this.id = id;
        this.email = email;
    }
    public Long getId() {
        return id;
    }
    public String getEmail() {
        return email;
    }
}