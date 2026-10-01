package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.domain.user.Password;
import com.jeannimi.messenger.domain.user.PasswordHash;

public interface PasswordHashPort {

  PasswordHash hash(Password password);

  boolean matches(Password password, PasswordHash passwordHash);
}
