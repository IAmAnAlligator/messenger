package com.jeannimi.messenger.adapter.out.persistence;


import com.jeannimi.messenger.application.port.out.ProcessedMessageRepositoryPort;
import com.jeannimi.messenger.message.entity.ProcessedMessage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProcessedMessageRepository implements ProcessedMessageRepositoryPort {

  private final ProcessedMessageJpaRepository processedMessageJpaRepository;

  @Override
  public boolean existsByEventId(UUID eventId) {
    return processedMessageJpaRepository.existsByEventId(eventId);
  }

  @Override
  public ProcessedMessage save(ProcessedMessage processedMessage) {
    return processedMessageJpaRepository.save(processedMessage);
  }
}
