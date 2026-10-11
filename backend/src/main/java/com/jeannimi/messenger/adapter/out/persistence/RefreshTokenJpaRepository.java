package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.adapter.out.persistence.entity.RefreshTokenEntity;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, UUID> {

  Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      """
            SELECT token
            FROM RefreshTokenEntity token
            WHERE token.tokenHash = :tokenHash
            """)
  Optional<RefreshTokenEntity> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

  List<RefreshTokenEntity> findAllBySessionId(UUID sessionId);

  @Modifying
  @Query(
      """
            UPDATE RefreshTokenEntity token
            SET token.revokedAt = :revokedAt
            WHERE token.sessionId = :sessionId
              AND token.revokedAt IS NULL
              AND token.expiresAt > :revokedAt
            """)
  int revokeActiveBySessionId(
      @Param("sessionId") UUID sessionId, @Param("revokedAt") Instant revokedAt);

  @Modifying
  @Query(
      """
            UPDATE RefreshTokenEntity token
            SET token.revokedAt = :revokedAt
            WHERE token.userId = :userId
              AND token.revokedAt IS NULL
              AND token.expiresAt > :revokedAt
            """)
  int revokeActiveByUserId(@Param("userId") UUID userId, @Param("revokedAt") Instant revokedAt);
}
