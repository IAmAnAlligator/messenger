package com.jeannimi.messenger.websocket.security;

import java.security.Principal;

public record WsUserPrincipal(Long userId, String username) implements Principal {

  @Override
  public String getName() {
    return username;
  }
}
