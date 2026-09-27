package com.jeannimi.messenger.adapter.out.user;

import com.jeannimi.messenger.application.port.out.HandleGeneratorPort;
import com.jeannimi.messenger.domain.user.Handle;
import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class RandomHandleGenerator implements HandleGeneratorPort  {

  private static final String CHARACTERS =
      "abcdefghijklmnopqrstuvwxyz0123456789";

  private final SecureRandom random = new SecureRandom();

  @Override
  public Handle generate() {

    StringBuilder value =
        new StringBuilder(Handle.HANDLE_LENGTH);

    for (int i = 0; i < Handle.HANDLE_LENGTH; i++) {
      value.append(
          CHARACTERS.charAt(
              random.nextInt(CHARACTERS.length())));
    }

    return new Handle(value.toString());
  }

}
