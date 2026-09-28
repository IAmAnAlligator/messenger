package com.jeannimi.messenger.adapter.out.security.password;

import com.jeannimi.messenger.application.port.out.PasswordHashPort;
import com.jeannimi.messenger.domain.user.Password;
import com.jeannimi.messenger.domain.user.PasswordHash;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordHashAdapter implements PasswordHashPort {

  private final PasswordEncoder passwordEncoder;

  @Override
  public PasswordHash hash(Password password) {
    return new PasswordHash(
        passwordEncoder.encode(password.getValue()));
  }

  @Override
  public boolean matches(
      Password password,
      PasswordHash passwordHash) {

    return passwordEncoder.matches(
        password.getValue(),
        passwordHash.getValue());
  }
}