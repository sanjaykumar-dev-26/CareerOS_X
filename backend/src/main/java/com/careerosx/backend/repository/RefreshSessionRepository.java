
package com.careerosx.backend.repository;

import com.careerosx.backend.entity.RefreshSession;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface RefreshSessionRepository
        extends JpaRepository<RefreshSession, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT rs FROM RefreshSession rs WHERE rs.tokenHash = :tokenHash")  
    Optional<RefreshSession> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);
    Optional<RefreshSession> findByTokenHash(String tokenHash);
    @Modifying
    @Query("UPDATE RefreshSession rs SET rs.revoked = true WHERE rs.user.id = :userId AND rs.revoked = false")
    int revokeAllByUserId(@Param("userId") Long userId);
}
