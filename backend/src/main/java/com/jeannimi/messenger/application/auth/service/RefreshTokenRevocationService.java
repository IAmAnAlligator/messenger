package com.jeannimi.messenger.application.auth.service;

import com.jeannimi.messenger.application.port.out.RefreshTokenRepositoryPort;
import com.jeannimi.messenger.domain.auth.SessionId;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenRevocationService {

  private final RefreshTokenRepositoryPort refreshTokenRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void revokeSession(SessionId sessionId, Instant now) {
    refreshTokenRepository.revokeSession(sessionId, now);
  }
}