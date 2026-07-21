package com.jeannimi.messenger.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannimi.messenger.dto.MessageDto;
import com.jeannimi.messenger.entity.OutboxEvent;
import com.jeannimi.messenger.entity.OutboxStatus;
import com.jeannimi.messenger.repository.OutboxRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

  // Репозиторий для работы с таблицей outbox_events.
  // Из неё забираются события, которые ещё не были отправлены в Kafka.
  private final OutboxRepository outboxRepository;

  // Kafka producer.
  // Используется для публикации сообщений в Kafka.
  //
  // String, String:
  // ключ сообщения → String
  // тело сообщения → String (JSON)
  private final KafkaTemplate<String, String> kafkaTemplate;

  private final ObjectMapper objectMapper;

  // Метод запускается автоматически каждые 1000 мс.
  //
  // fixedDelay = 1000 означает:
  // следующий запуск начнётся через 1 секунду
  // после завершения предыдущего.
  @Scheduled(fixedDelay = 1000)

  // Одна транзакция на весь запуск.
  //
  // Если внутри меняется статус события:
  // NEW → SENT
  // изменения будут сохранены автоматически
  // после завершения метода.
  @Transactional
  public void publishOutboxEvents() {

    // Получаем только новые события.
    // Сортировка по id нужна для стабильного порядка публикации.
    // Пример:
    // id=101 → отправится первым
    // id=102 → отправится вторым

    List<OutboxEvent> events = outboxRepository.findByStatusOrderByIdAsc(OutboxStatus.NEW);

    // Обрабатываем события по одному
    for (OutboxEvent event : events) {

      try {

        // Десериализуем JSON из outbox обратно в объект сообщения.
        MessageDto dto = objectMapper.readValue(event.getPayload(), MessageDto.class);

        // Для Kafka используется ключ = chatId.
        // Это гарантирует: сообщения одного чата попадут в один partition и сохранят порядок.
        if (dto.chatId() == null) {
          throw new IllegalStateException("chatId is null");
        }

        // Отправляем событие в Kafka.
        // .get() блокирует поток до получения подтверждения от broker.
        // Это важно: БЕЗ .get(): send() status=SENT приложение упало
        // → сообщение потеряно //
        // С .get(): сначала подтверждение Kafka потом статус SENT
        kafkaTemplate.send(event.getTopic(), dto.chatId().toString(), event.getPayload()).get();

        // Помечаем событие как отправленное.
        // Благодаря @Transactional отдельный save() не нужен — JPA сохранит изменения
        // автоматически.
        event.markSent();

        log.info("[OUTBOX SENT] event={}, chat={}", event.getId(), dto.chatId());

      } catch (Exception e) {

        // Любая ошибка: // - неправильный JSON - Kafka недоступна - timeout - отсутствует chatId
        // переводит событие в FAILED.
        log.error("[OUTBOX FAILED] event={}", event.getId(), e);

        event.markFailed();
      }

      // После завершения метода: Spring выполнит COMMIT.
      // Все статусы SENT / FAILED // сохранятся в БД.
    }
  }
}
