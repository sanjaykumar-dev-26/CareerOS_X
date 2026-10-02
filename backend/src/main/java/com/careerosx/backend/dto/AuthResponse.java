package com.careerosx.backend.dto;

public class AuthResponse {
    private String accessToken;
    private String refreshToken;


    public AuthResponse(String acessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
    public String getAcessToken() {
        return accessToken;
    }
    public String getRefreshToken() {
        return refreshToken;
    }               
   
}
