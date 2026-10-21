package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.domain.notification.WebPushSubscription;
import com.jeannimi.messenger.domain.user.UserId;
import java.util.List;
import java.util.Optional;

public interface WebPushSubscriptionRepositoryPort {

  WebPushSubscription save(WebPushSubscription subscription);

  List<WebPushSubscription> findAllByUserId(UserId userId);

  void deleteByUserIdAndEndpoint(UserId userId, String endpoint);

  Optional<WebPushSubscription> findByEndpoint(String endpoint);
}
