package com.jeannimi.messenger.adapter.out.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannimi.messenger.application.notification.dto.MessageNotification;
import com.jeannimi.messenger.application.port.out.WebPushNotificationPort;
import com.jeannimi.messenger.application.port.out.WebPushSubscriptionRepositoryPort;
import com.jeannimi.messenger.domain.chat.ChatType;
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

    log.debug(
        "Found {} Web Push subscriptions for {} recipients",
        subscriptions.size(),
        notification.recipientUserIds().size());

    if (subscriptions.isEmpty()) {
      log.debug("No Web Push subscriptions found");
      return;
    }

    byte[] payload = createPayload(notification);

    RuntimeException deliveryFailure = null;

    for (WebPushSubscription subscription : subscriptions) {
      try {
        sendToSubscription(subscription, payload);
      } catch (RuntimeException exception) {
        if (deliveryFailure == null) {
          deliveryFailure = new IllegalStateException(
              "Failed to deliver Web Push notification to all subscriptions");
        }

        deliveryFailure.addSuppressed(exception);
      }
    }

    if (deliveryFailure != null) {
      throw deliveryFailure;
    }
  }

  private byte[] createPayload(MessageNotification notification) {
    PushPayload payload = switch (notification.chatType()) {
      case PRIVATE -> preparePrivatePayload(notification);
      case GROUP -> prepareGroupPayload(notification);
    };

    try {
      return objectMapper.writeValueAsBytes(payload);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException(
          "Failed to serialize Web Push payload",
          exception);
    }
  }

  private PushPayload preparePrivatePayload(MessageNotification notification) {
    return new PushPayload(
        notification.senderUsername(),
        "Sent you a new message",
        createPushData(notification));
  }

  private PushPayload prepareGroupPayload(MessageNotification notification) {
    return new PushPayload(
        notification.chatName(),
        notification.senderUsername() + " sent a message",
        createPushData(notification));
  }

  private PushData createPushData(MessageNotification notification) {
    return new PushData(
        notification.chatId().value().toString(),
        notification.messageId().value().toString());
  }

  private void sendToSubscription(
      WebPushSubscription subscription,
      byte[] payload) {

    try {
      Notification pushNotification = new Notification(
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
        String responseBody = "";

        if (response.getEntity() != null) {
          responseBody =
              org.apache.http.util.EntityUtils.toString(response.getEntity());
        }

        throw new IllegalStateException(
            "Web Push failed: status=" + status
                + ", responseBody=" + responseBody);
      }

      log.debug(
          "Web Push accepted by push service: status={}, subscriptionId={}",
          status,
          subscription.getId().value());

    } catch (Exception exception) {
      throw new IllegalStateException(
          "Web Push delivery failed for subscription "
              + subscription.getId().value(),
          exception);
    }
  }

  private void deleteInvalidSubscription(WebPushSubscription subscription) {
    subscriptionRepository.deleteByUserIdAndEndpoint(
        subscription.getUserId(),
        subscription.getEndpoint());

    log.info(
        "Deleted invalid Web Push subscription {}",
        subscription.getId().value());
  }

  private record PushPayload(String title, String body, PushData data) {}

  private record PushData(String chatId, String messageId) {}
}