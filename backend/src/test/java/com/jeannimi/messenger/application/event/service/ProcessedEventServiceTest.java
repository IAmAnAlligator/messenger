package com.jeannimi.messenger.application.event.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.jeannimi.messenger.application.port.out.repository.ProcessedEventRepositoryPort;
import com.jeannimi.messenger.domain.common.EventId;
import com.jeannimi.messenger.domain.event.ProcessedEvent;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessedEventServiceTest {

  private static final EventId EVENT_ID =
      new EventId(UUID.fromString("01912345-6789-7000-8000-000000000001"));

  @Mock private ProcessedEventRepositoryPort processedEventRepository;

  @InjectMocks private ProcessedEventService processedEventService;

  @Test
  void existsByEventId_shouldReturnTrue_whenEventExists() {
    when(processedEventRepository.existsByEventId(EVENT_ID)).thenReturn(true);

    boolean result = processedEventService.existsByEventId(EVENT_ID);

    assertThat(result).isTrue();

    verify(processedEventRepository).existsByEventId(EVENT_ID);
    verifyNoMoreInteractions(processedEventRepository);
  }

  @Test
  void existsByEventId_shouldReturnFalse_whenEventDoesNotExist() {
    when(processedEventRepository.existsByEventId(EVENT_ID)).thenReturn(false);

    boolean result = processedEventService.existsByEventId(EVENT_ID);

    assertThat(result).isFalse();

    verify(processedEventRepository).existsByEventId(EVENT_ID);
    verifyNoMoreInteractions(processedEventRepository);
  }

  @Test
  void existsByEventId_shouldThrowException_whenEventIdIsNull() {
    assertThatNullPointerException()
        .isThrownBy(() -> processedEventService.existsByEventId(null))
        .withMessage("eventId");

    org.mockito.Mockito.verifyNoInteractions(processedEventRepository);
  }

  @Test
  void register_shouldSaveProcessedEvent() {
    processedEventService.register(EVENT_ID);

    ArgumentCaptor<ProcessedEvent> captor = ArgumentCaptor.forClass(ProcessedEvent.class);

    verify(processedEventRepository).save(captor.capture());

    assertThat(captor.getValue()).isNotNull();

    verifyNoMoreInteractions(processedEventRepository);
  }

  @Test
  void register_shouldThrowException_whenEventIdIsNull() {
    assertThatNullPointerException()
        .isThrownBy(() -> processedEventService.register(null))
        .withMessage("eventId");

    org.mockito.Mockito.verifyNoInteractions(processedEventRepository);
  }
}
