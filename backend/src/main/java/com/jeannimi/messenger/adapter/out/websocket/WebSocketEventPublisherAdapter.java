package com.jeannimi.messenger.adapter.out.websocket;

import com.jeannimi.messenger.adapter.out.websocket.dto.ChatCreatedWebSocketDto;
import com.jeannimi.messenger.adapter.out.websocket.dto.ChatDeletedWebSocketDto;
import com.jeannimi.messenger.adapter.out.websocket.dto.ChatMemberAddedWebSocketDto;
import com.jeannimi.messenger.adapter.out.websocket.dto.ChatMemberLeftWebSocketDto;
import com.jeannimi.messenger.adapter.out.websocket.dto.ChatMemberRemovedWebSocketDto;
import com.jeannimi.messenger.adapter.out.websocket.dto.ChatRenamedWebSocketDto;
import com.jeannimi.messenger.adapter.out.websocket.dto.MessageCreatedWebSocketDto;
import com.jeannimi.messenger.adapter.out.websocket.dto.MessageDeletedWebSocketDto;
import com.jeannimi.messenger.adapter.out.websocket.dto.MessageReadWebSocketDto;
import com.jeannimi.messenger.adapter.out.websocket.dto.WebSocketEvent;
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
import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.user.UserId;
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
    MessageCreatedWebSocketDto payload = MessageCreatedWebSocketDto.from(event);
    WebSocketEvent<MessageCreatedWebSocketDto> webSocketEvent =
        WebSocketEvent.of(EventType.MESSAGE_CREATED, payload);
    sendToChat(event.message().chatId(), webSocketEvent);
    sendToUsers(event.recipientUserIds(), webSocketEvent);
  }

  private void publishMessageRead(MessageReadEvent event) {
    MessageReadWebSocketDto payload = MessageReadWebSocketDto.from(event);
    WebSocketEvent<MessageReadWebSocketDto> webSocketEvent =
        WebSocketEvent.of(EventType.MESSAGE_READ, payload);
    sendToChat(event.chatId(), webSocketEvent);
  }

  private void publishMessageDeleted(MessageDeletedEvent event) {
    MessageDeletedWebSocketDto payload = MessageDeletedWebSocketDto.from(event);
    WebSocketEvent<MessageDeletedWebSocketDto> webSocketEvent =
        WebSocketEvent.of(EventType.MESSAGE_DELETED, payload);
    sendToChat(event.chatId(), webSocketEvent);
  }

  private void publishChatCreated(ChatCreatedEvent event) {
    ChatCreatedWebSocketDto payload = ChatCreatedWebSocketDto.from(event);
    WebSocketEvent<ChatCreatedWebSocketDto> webSocketEvent =
        WebSocketEvent.of(EventType.CHAT_CREATED, payload);
    messagingTemplate.convertAndSend("/topic/chat.created", webSocketEvent);
    sendToUsers(event.memberIds(), webSocketEvent);
  }

  private void publishChatDeleted(ChatDeletedEvent event) {
    ChatDeletedWebSocketDto payload = ChatDeletedWebSocketDto.from(event);
    WebSocketEvent<ChatDeletedWebSocketDto> webSocketEvent =
        WebSocketEvent.of(EventType.CHAT_DELETED, payload);
    sendToChat(event.chatId(), webSocketEvent);
    messagingTemplate.convertAndSend("/topic/chat.deleted", webSocketEvent);
    sendToUsers(event.recipientUserIds(), webSocketEvent);
  }

  private void publishChatMemberAdded(ChatMemberAddedEvent event) {
    ChatMemberAddedWebSocketDto payload = ChatMemberAddedWebSocketDto.from(event);
    WebSocketEvent<ChatMemberAddedWebSocketDto> webSocketEvent =
        WebSocketEvent.of(EventType.CHAT_MEMBER_ADDED, payload);
    sendToChat(event.chatId(), webSocketEvent);
    sendToUser(event.userId(), webSocketEvent);
  }

  private void publishChatMemberRemoved(ChatMemberRemovedEvent event) {
    ChatMemberRemovedWebSocketDto payload = ChatMemberRemovedWebSocketDto.from(event);
    WebSocketEvent<ChatMemberRemovedWebSocketDto> webSocketEvent =
        WebSocketEvent.of(EventType.CHAT_MEMBER_REMOVED, payload);
    sendToChat(event.chatId(), webSocketEvent);
    sendToUser(event.userId(), webSocketEvent);
  }

  private void publishChatMemberLeft(ChatMemberLeftEvent event) {
    ChatMemberLeftWebSocketDto payload = ChatMemberLeftWebSocketDto.from(event);
    WebSocketEvent<ChatMemberLeftWebSocketDto> webSocketEvent =
        WebSocketEvent.of(EventType.CHAT_MEMBER_LEFT, payload);
    sendToChat(event.chatId(), webSocketEvent);
    sendToUser(event.userId(), webSocketEvent);
  }

  private void publishChatRenamed(ChatRenamedEvent event) {
    ChatRenamedWebSocketDto payload = ChatRenamedWebSocketDto.from(event);
    WebSocketEvent<ChatRenamedWebSocketDto> webSocketEvent =
        WebSocketEvent.of(EventType.CHAT_RENAMED, payload);
    sendToChat(event.chatId(), webSocketEvent);
    sendToUsers(event.recipientUserIds(), webSocketEvent);
  }

  private void sendToChat(ChatId chatId, Object event) {
    messagingTemplate.convertAndSend("/topic/chat/" + chatId.value(), event);
  }

  private void sendToUser(UserId userId, Object event) {
    messagingTemplate.convertAndSend("/topic/user/" + userId.value() + "/chats", event);
  }

  private void sendToUsers(List<UserId> userIds, Object event) {
    for (UserId userId : userIds) {
      sendToUser(userId, event);
    }
  }
}
