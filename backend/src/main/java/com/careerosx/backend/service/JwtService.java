
package com.careerosx.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(keyBytes);
    }
    // Generate access token (15 minutes)
    public String generateToken(String email, int tokenVersion) {
        return Jwts.builder()
                .subject(email)
                .claim("tokenVersion", tokenVersion)
                .claim("tokenType", "access")
                .expiration(new Date(System.currentTimeMillis() + 15L * 60 * 1000))
                .signWith(key)
                .compact();
    }
    // Generate refresh token (7 days)
    public String generateRefreshToken(String email, int tokenVersion) {
        return Jwts.builder()
                .subject(email)
                .claim("tokenVersion", tokenVersion)
                .claim("tokenType", "refresh")
                .expiration(new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000))
                .signWith(key)
                .compact();
    }

    // Check token signature and expiry
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Extract email from token
    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Extract token version from token
    public int extractTokenVersion(String token) {
        Object version = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("tokenVersion");

        return ((Number) version).intValue();
    }

    // Extract token type
    public String extractTokenType(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("tokenType", String.class);
    }
    // Check whether token is specifically a refresh token
    public boolean isRefreshToken(String token) {
        try {
            return isTokenValid(token)
                    && "refresh".equals(extractTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }
    public String hashToken(String token) {
    try {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(
                token.getBytes(StandardCharsets.UTF_8)
        );

        return java.util.HexFormat.of().formatHex(hashBytes);
    } catch (NoSuchAlgorithmException e) {
        throw new IllegalStateException("SHA-256 is not available", e);
    }
}

public Instant getRefreshTokenExpiry(String refreshToken) {
    Date expiration = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(refreshToken)
            .getPayload()
            .getExpiration();

    return expiration.toInstant();
    }
}
