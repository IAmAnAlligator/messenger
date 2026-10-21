package com.jeannimi.messenger.application.port.in;

import com.jeannimi.messenger.application.notification.dto.WebPushSubscriptionResult;
import com.jeannimi.messenger.domain.user.UserId;
import java.util.List;

public interface WebPushSubscriptionUseCase {

  WebPushSubscriptionResult register(UserId userId, String endpoint, String p256dh, String auth);

  List<WebPushSubscriptionResult> getSubscriptions(UserId userId);

  void delete(UserId userId, String endpoint);
}
