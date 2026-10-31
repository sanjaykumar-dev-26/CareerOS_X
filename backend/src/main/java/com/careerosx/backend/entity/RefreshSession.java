
package com.careerosx.backend.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "refresh_sessions")
public class RefreshSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;
    public RefreshSession() {
    }
    public RefreshSession(User user, String tokenHash, Instant expiresAt) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }
    public Long getId() {
        return id;
    }
    public User getUser() {
        return user;
    }
    public String getTokenHash() {
        return tokenHash;
    }
    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }
    public Instant getExpiresAt() {
        return expiresAt;
    }
    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
    public boolean isRevoked() {
        return revoked;
    }
    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }
}
