package com.jeannimi.messenger.adapter.out.persistence.mapper;

import com.jeannimi.messenger.adapter.out.persistence.entity.RefreshTokenEntity;
import com.jeannimi.messenger.domain.auth.RefreshToken;
import com.jeannimi.messenger.domain.auth.RefreshTokenId;
import com.jeannimi.messenger.domain.auth.SessionId;
import com.jeannimi.messenger.domain.user.UserId;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenMapper {

  public RefreshTokenEntity toEntity(RefreshToken token) {

    return new RefreshTokenEntity(
        token.getId().value(),
        token.getSessionId().value(),
        token.getUserId().value(),
        token.getTokenHash(),
        token.getCreatedAt(),
        token.getExpiresAt(),
        token.getRevokedAt(),
        token.getReplacedByTokenId() == null ? null : token.getReplacedByTokenId().value());
  }

  public RefreshToken toDomain(RefreshTokenEntity entity) {

    return RefreshToken.reconstitute(
        new RefreshTokenId(entity.getId()),
        new SessionId(entity.getSessionId()),
        new UserId(entity.getUserId()),
        entity.getTokenHash(),
        entity.getCreatedAt(),
        entity.getExpiresAt(),
        entity.getRevokedAt(),
        entity.getReplacedByTokenId() == null
            ? null
            : new RefreshTokenId(entity.getReplacedByTokenId()));
  }
}
