package com.jeannimi.messenger.security.websocker;

import java.security.Principal;

public record WsUserPrincipal(Long userId, String username) implements Principal {

  @Override
  public String getName() {
    return username;
  }
}
