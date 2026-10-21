package com.jeannimi.messenger.adapter.out.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannimi.messenger.application.notification.dto.MessageNotification;
import com.jeannimi.messenger.application.port.out.WebPushNotificationPort;
import com.jeannimi.messenger.application.port.out.WebPushSubscriptionRepositoryPort;
import com.jeannimi.messenger.domain.notification.WebPushSubscription;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebPushNotificationAdapter implements WebPushNotificationPort {

  private static final int TTL_SECONDS = 3600;

  private final PushService pushService;
  private final ObjectMapper objectMapper;
  private final WebPushSubscriptionRepositoryPort subscriptionRepository;

  @Override
  public void send(MessageNotification notification) {
    List<WebPushSubscription> subscriptions =
        notification.recipientUserIds().stream()
            .flatMap(userId -> subscriptionRepository.findAllByUserId(userId).stream())
            .toList();

    if (subscriptions.isEmpty()) {
      return;
    }

    byte[] payload;

    try {
      payload =
          objectMapper.writeValueAsBytes(
              new PushPayload(
                  notification.senderUsername(),
                  "Новое сообщение",
                  new PushData(
                      notification.chatId().value().toString(),
                      notification.messageId().value().toString())));
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize Web Push payload", exception);
    }

    for (WebPushSubscription subscription : subscriptions) {
      sendToSubscription(subscription, payload);
    }
  }

  private void sendToSubscription(WebPushSubscription subscription, byte[] payload) {

    try {
      Notification pushNotification =
          new Notification(
              subscription.getEndpoint(),
              subscription.getP256dh(),
              subscription.getAuth(),
              payload,
              TTL_SECONDS);

      HttpResponse response = pushService.send(pushNotification);
      int status = response.getStatusLine().getStatusCode();

      if (status == 404 || status == 410) {
        deleteInvalidSubscription(subscription);
        return;
      }

      if (status < 200 || status >= 300) {
        log.warn(
            "Web Push failed with HTTP status {} for subscription {}",
            status,
            subscription.getId().value());
        return;
      }

      log.debug("Web Push sent successfully for subscription {}", subscription.getId().value());
    } catch (Exception exception) {
      log.warn(
          "Web Push delivery failed for subscription {}", subscription.getId().value(), exception);
    }
  }

  private void deleteInvalidSubscription(WebPushSubscription subscription) {
    try {
      subscriptionRepository.deleteByUserIdAndEndpoint(
          subscription.getUserId(), subscription.getEndpoint());

      log.info("Deleted invalid Web Push subscription {}", subscription.getId().value());
    } catch (Exception exception) {
      log.error(
          "Failed to delete invalid Web Push subscription {}",
          subscription.getId().value(),
          exception);
    }
  }

  private record PushPayload(String title, String body, PushData data) {}

  private record PushData(String chatId, String messageId) {}
}
