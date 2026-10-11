package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.adapter.out.persistence.entity.RefreshTokenEntity;
import com.jeannimi.messenger.adapter.out.persistence.mapper.RefreshTokenMapper;
import com.jeannimi.messenger.application.auth.dto.RotationResult;
import com.jeannimi.messenger.application.port.out.RefreshTokenRepositoryPort;
import com.jeannimi.messenger.domain.auth.RefreshToken;
import com.jeannimi.messenger.domain.auth.RefreshTokenId;
import com.jeannimi.messenger.domain.auth.SessionId;
import com.jeannimi.messenger.domain.user.UserId;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenPersistenceAdapter implements RefreshTokenRepositoryPort {

  private final RefreshTokenJpaRepository repository;
  private final RefreshTokenMapper refreshTokenMapper;

  @Override
  public void save(RefreshToken refreshToken) {
    repository.save(refreshTokenMapper.toEntity(refreshToken));
  }

  @Override
  public Optional<RefreshToken> findByTokenHash(String tokenHash) {
    return repository.findByTokenHash(tokenHash).map(refreshTokenMapper::toDomain);
  }

  @Override
  public RotationResult rotate(String currentTokenHash, RefreshToken replacement, Instant now) {
    Optional<RefreshTokenEntity> optionalCurrent =
        repository.findByTokenHashForUpdate(currentTokenHash);

    if (optionalCurrent.isEmpty()) {
      return RotationResult.NOT_FOUND;
    }

    RefreshTokenEntity currentEntity = optionalCurrent.get();

    RefreshToken current = refreshTokenMapper.toDomain(currentEntity);

    if (current.isRevoked()) {
      repository.revokeActiveBySessionId(currentEntity.getSessionId(), now);

      return RotationResult.REUSE_DETECTED;
    }

    if (current.isExpired(now)) {
      return RotationResult.EXPIRED;
    }

    if (!current.getSessionId().equals(replacement.getSessionId())) {
      throw new IllegalArgumentException("Replacement must belong to the same session");
    }

    if (!current.getUserId().equals(replacement.getUserId())) {
      throw new IllegalArgumentException("Replacement must belong to the same user");
    }

    current.replaceWith(replacement.getId(), now);

    currentEntity.updateState(
        current.getRevokedAt(),
        current.getReplacedByTokenId() == null ? null : current.getReplacedByTokenId().value());

    repository.save(currentEntity);

    repository.save(refreshTokenMapper.toEntity(replacement));

    return RotationResult.ROTATED;
  }

  @Override
  public int revokeSession(SessionId sessionId, Instant revokedAt) {
    return repository.revokeActiveBySessionId(sessionId.value(), revokedAt);
  }

  @Override
  public int revokeAllActiveByUserId(UserId userId, Instant revokedAt) {
    return repository.revokeActiveByUserId(userId.value(), revokedAt);
  }

  @Override
  public Optional<RefreshToken> findById(RefreshTokenId id) {
    return repository.findById(id.value()).map(refreshTokenMapper::toDomain);
  }
}
