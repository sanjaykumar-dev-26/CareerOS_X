package com.careerosx.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import io.jsonwebtoken.Jwts;

import java.util.Base64;

@Service 
public class JwtService {
    @Value("${jwt.secret}")
    private String secretKey ;
    private SecretKey key;
    @PostConstruct
    public void init() {   
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(keyBytes);
    }
    public String generateToken(String email) {
       return Jwts.builder().subject(email).expiration(new Date(System.currentTimeMillis() +  15L * 60 * 1000)).signWith(key).compact();
    }
    public String generateRefreshToken(String email) {
        return Jwts.builder().subject(email).expiration(new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000)).signWith(key).compact();
    }

}