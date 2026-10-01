package com.jeannimi.messenger.adapter.out.id;

import com.github.f4b6a3.uuid.UuidCreator;
import com.jeannimi.messenger.application.port.out.IdGenerator;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UuidV7Generator implements IdGenerator {

  @Override
  public UUID generate() {
    return UuidCreator.getTimeOrderedEpoch();
  }
}
