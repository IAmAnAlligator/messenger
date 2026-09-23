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
import java.util.List;
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
      case MESSAGE_CREATED -> publishMessageCreated((MessageCreatedEvent) event);

      case MESSAGE_READ -> publishMessageRead((MessageReadEvent) event);

      case MESSAGE_DELETED -> publishMessageDeleted((MessageDeletedEvent) event);

      case CHAT_CREATED -> publishChatCreated((ChatCreatedEvent) event);

      case CHAT_DELETED -> publishChatDeleted((ChatDeletedEvent) event);

      case CHAT_MEMBER_ADDED -> publishChatMemberAdded((ChatMemberAddedEvent) event);

      case CHAT_MEMBER_REMOVED -> publishChatMemberRemoved((ChatMemberRemovedEvent) event);

      case CHAT_MEMBER_LEFT -> publishChatMemberLeft((ChatMemberLeftEvent) event);

      case CHAT_RENAMED -> publishChatRenamed((ChatRenamedEvent) event);

      default -> throw new IllegalArgumentException("Unsupported realtime event: " + eventType);
    }
  }

  private void publishMessageCreated(MessageCreatedEvent event) {

    MessageDto messageDto = MessageDto.fromResult(event.message());

    WebSocketEvent<MessageDto> webSocketEvent =
        WebSocketEvent.of(EventType.MESSAGE_CREATED, messageDto);

    sendToChat(messageDto.chatId(), webSocketEvent);

    sendToUsers(event.recipientUserIds(), webSocketEvent);
  }

  private void publishMessageRead(MessageReadEvent event) {

    WebSocketEvent<MessageReadEvent> webSocketEvent =
        WebSocketEvent.of(EventType.MESSAGE_READ, event);

    sendToChat(event.chatId(), webSocketEvent);
  }

  private void publishMessageDeleted(MessageDeletedEvent event) {

    WebSocketEvent<MessageDeletedEvent> webSocketEvent =
        WebSocketEvent.of(EventType.MESSAGE_DELETED, event);

    sendToChat(event.chatId(), webSocketEvent);
  }

  private void publishChatCreated(ChatCreatedEvent event) {

    WebSocketEvent<ChatCreatedEvent> webSocketEvent =
        WebSocketEvent.of(EventType.CHAT_CREATED, event);

    messagingTemplate.convertAndSend("/topic/chat.created", webSocketEvent);

    sendToUsers(event.memberIds(), webSocketEvent);
  }

  private void publishChatDeleted(ChatDeletedEvent event) {

    WebSocketEvent<ChatDeletedEvent> webSocketEvent =
        WebSocketEvent.of(EventType.CHAT_DELETED, event);

    sendToChat(event.chatId(), webSocketEvent);

    messagingTemplate.convertAndSend("/topic/chat.deleted", webSocketEvent);

    sendToUsers(event.recipientUserIds(), webSocketEvent);
  }

  private void publishChatMemberAdded(ChatMemberAddedEvent event) {

    WebSocketEvent<ChatMemberAddedEvent> webSocketEvent =
        WebSocketEvent.of(EventType.CHAT_MEMBER_ADDED, event);

    sendToChat(event.chatId(), webSocketEvent);

    sendToUser(event.userId(), webSocketEvent);
  }

  private void publishChatMemberRemoved(ChatMemberRemovedEvent event) {

    WebSocketEvent<ChatMemberRemovedEvent> webSocketEvent =
        WebSocketEvent.of(EventType.CHAT_MEMBER_REMOVED, event);

    sendToChat(event.chatId(), webSocketEvent);

    sendToUser(event.userId(), webSocketEvent);
  }

  private void publishChatMemberLeft(ChatMemberLeftEvent event) {

    WebSocketEvent<ChatMemberLeftEvent> webSocketEvent =
        WebSocketEvent.of(EventType.CHAT_MEMBER_LEFT, event);

    sendToChat(event.chatId(), webSocketEvent);

    sendToUser(event.userId(), webSocketEvent);
  }

  private void publishChatRenamed(ChatRenamedEvent event) {

    WebSocketEvent<ChatRenamedEvent> webSocketEvent =
        WebSocketEvent.of(EventType.CHAT_RENAMED, event);

    sendToChat(event.chatId(), webSocketEvent);

    sendToUsers(event.recipientUserIds(), webSocketEvent);
  }

  private void sendToChat(Long chatId, Object event) {

    messagingTemplate.convertAndSend("/topic/chat/" + chatId, event);
  }

  private void sendToUser(Long userId, Object event) {

    messagingTemplate.convertAndSend("/topic/user/" + userId + "/chats", event);
  }

  private void sendToUsers(List<Long> userIds, Object event) {

    for (Long userId : userIds) {
      sendToUser(userId, event);
    }
  }
}
