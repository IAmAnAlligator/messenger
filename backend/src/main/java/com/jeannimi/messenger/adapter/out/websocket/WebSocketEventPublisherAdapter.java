package com.jeannimi.messenger.adapter.out.websocket;

import com.jeannimi.messenger.adapter.in.web.message.dto.MessageDto;
import com.jeannimi.messenger.adapter.in.websocket.WebSocketEvent;
import com.jeannimi.messenger.application.event.ApplicationEvent;
import com.jeannimi.messenger.application.event.ChatCreatedEvent;
import com.jeannimi.messenger.application.event.ChatDeletedEvent;
import com.jeannimi.messenger.application.event.ChatMemberAddedEvent;
import com.jeannimi.messenger.application.event.ChatMemberLeftEvent;
import com.jeannimi.messenger.application.event.ChatMemberRemovedEvent;
import com.jeannimi.messenger.application.event.ChatRenamedEvent;
import com.jeannimi.messenger.application.event.EventType;
import com.jeannimi.messenger.application.event.MessageCreatedEvent;
import com.jeannimi.messenger.application.event.MessageDeletedEvent;
import com.jeannimi.messenger.application.event.MessageReadEvent;
import com.jeannimi.messenger.application.port.out.RealtimeEventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketEventPublisherAdapter implements RealtimeEventPublisherPort {

  private final SimpMessagingTemplate messagingTemplate;

  @Override
  public void publish(EventType eventType, ApplicationEvent event) {

    switch (eventType) {
      case MESSAGE_DELETED -> {
        MessageDeletedEvent messageDeletedEvent = (MessageDeletedEvent) event;

        WebSocketEvent<MessageDeletedEvent> webSocketEvent =
            WebSocketEvent.of(eventType, messageDeletedEvent);

        messagingTemplate.convertAndSend(
            "/topic/chat/" + messageDeletedEvent.chatId(), webSocketEvent);
      }

      case MESSAGE_READ -> {
        MessageReadEvent messageReadEvent = (MessageReadEvent) event;

        WebSocketEvent<MessageReadEvent> webSocketEvent =
            WebSocketEvent.of(eventType, messageReadEvent);

        messagingTemplate.convertAndSend(
            "/topic/chat/" + messageReadEvent.chatId(), webSocketEvent);
      }

      case MESSAGE_CREATED -> {
        MessageCreatedEvent messageCreatedEvent = (MessageCreatedEvent) event;

        MessageDto messageDto = MessageDto.fromResult(messageCreatedEvent.message());

        WebSocketEvent<MessageDto> webSocketEvent = WebSocketEvent.of(eventType, messageDto);

        messagingTemplate.convertAndSend("/topic/chat/" + messageDto.chatId(), webSocketEvent);
      }

      case CHAT_CREATED -> {
        ChatCreatedEvent chatCreatedEvent = (ChatCreatedEvent) event;

        WebSocketEvent<ChatCreatedEvent> webSocketEvent =
            WebSocketEvent.of(eventType, chatCreatedEvent);

        /*
         * Уведомляем подписчиков,
         * которые слушают создание чатов.
         */
        messagingTemplate.convertAndSend("/topic/chat.created", webSocketEvent);

        /*
         * Обновляем список чатов
         * у каждого пользователя.
         */
        for (Long memberId : chatCreatedEvent.memberIds()) {
          messagingTemplate.convertAndSend("/topic/user/" + memberId + "/chats", webSocketEvent);
        }
      }

      case CHAT_DELETED -> {
        ChatDeletedEvent chatDeletedEvent = (ChatDeletedEvent) event;

        WebSocketEvent<ChatDeletedEvent> webSocketEvent =
            WebSocketEvent.of(eventType, chatDeletedEvent);

        /*
         * Сообщаем участникам,
         * что чат удалён.
         */
        messagingTemplate.convertAndSend(
            "/topic/chat/" + chatDeletedEvent.chatId(), webSocketEvent);

        /*
         * Глобальное событие удаления.
         */
        messagingTemplate.convertAndSend("/topic/chat.deleted", webSocketEvent);
      }

      case CHAT_MEMBER_ADDED -> {
        ChatMemberAddedEvent chatMemberAddedEvent = (ChatMemberAddedEvent) event;

        WebSocketEvent<ChatMemberAddedEvent> webSocketEvent =
            WebSocketEvent.of(eventType, chatMemberAddedEvent);

        /*
         * Уведомляем участников чата.
         */
        messagingTemplate.convertAndSend(
            "/topic/chat/" + chatMemberAddedEvent.chatId(), webSocketEvent);

        /*
         * Отдельно уведомляем нового пользователя,
         * чтобы обновить список чатов.
         */
        messagingTemplate.convertAndSend(
            "/topic/user/" + chatMemberAddedEvent.userId() + "/chats", webSocketEvent);
      }

      case CHAT_MEMBER_REMOVED -> {
        ChatMemberRemovedEvent chatMemberRemovedEvent = (ChatMemberRemovedEvent) event;

        WebSocketEvent<ChatMemberRemovedEvent> webSocketEvent =
            WebSocketEvent.of(eventType, chatMemberRemovedEvent);

        /*
         * Уведомляем участников чата.
         */
        messagingTemplate.convertAndSend(
            "/topic/chat/" + chatMemberRemovedEvent.chatId(), webSocketEvent);

        /*
         * Обновляем список чатов
         * удалённого пользователя.
         */
        messagingTemplate.convertAndSend(
            "/topic/user/" + chatMemberRemovedEvent.userId() + "/chats", webSocketEvent);
      }

      case CHAT_MEMBER_LEFT -> {
        ChatMemberLeftEvent chatMemberLeftEvent = (ChatMemberLeftEvent) event;

        WebSocketEvent<ChatMemberLeftEvent> webSocketEvent =
            WebSocketEvent.of(eventType, chatMemberLeftEvent);

        messagingTemplate.convertAndSend(
            "/topic/chat/" + chatMemberLeftEvent.chatId(), webSocketEvent);

        messagingTemplate.convertAndSend(
            "/topic/user/" + chatMemberLeftEvent.userId() + "/chats", webSocketEvent);
      }

      case CHAT_RENAMED -> {
        ChatRenamedEvent chatRenamedEvent = (ChatRenamedEvent) event;

        WebSocketEvent<ChatRenamedEvent> webSocketEvent =
            WebSocketEvent.of(eventType, chatRenamedEvent);

        messagingTemplate.convertAndSend(
            "/topic/chat/" + chatRenamedEvent.chatId(), webSocketEvent);
      }

      default -> throw new IllegalArgumentException("Unsupported realtime event: " + eventType);
    }
  }
}
