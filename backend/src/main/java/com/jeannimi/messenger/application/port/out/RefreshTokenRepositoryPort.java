package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.application.auth.dto.RotationResult;
import com.jeannimi.messenger.domain.auth.RefreshToken;
import com.jeannimi.messenger.domain.auth.RefreshTokenId;
import com.jeannimi.messenger.domain.auth.SessionId;
import com.jeannimi.messenger.domain.user.UserId;
import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepositoryPort {

  void save(RefreshToken refreshToken);

  Optional<RefreshToken> findByTokenHash(String tokenHash);

  RotationResult rotate(String currentTokenHash, RefreshToken replacement, Instant now);

  int revokeSession(SessionId sessionId, Instant revokedAt);

  int revokeAllActiveByUserId(UserId userId, Instant revokedAt);

  Optional<RefreshToken> findById(RefreshTokenId id);
}
