package com.jeannimi.messenger.application.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.jeannimi.messenger.application.notification.dto.WebPushSubscriptionResult;
import com.jeannimi.messenger.application.port.out.common.IdGenerator;
import com.jeannimi.messenger.application.port.out.repository.WebPushSubscriptionRepositoryPort;
import com.jeannimi.messenger.domain.notification.WebPushSubscription;
import com.jeannimi.messenger.domain.notification.WebPushSubscriptionId;
import com.jeannimi.messenger.domain.user.UserId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WebPushSubscriptionServiceTest {

  @Mock private WebPushSubscriptionRepositoryPort subscriptionRepository;

  @Mock private IdGenerator idGenerator;

  @InjectMocks private WebPushSubscriptionService service;

  private UserId userId;
  private UserId anotherUserId;

  private UUID subscriptionUuid;
  private UUID existingSubscriptionUuid;

  private static final String ENDPOINT = "https://push.example.com/subscription/123";
  private static final String P256DH = "test-p256dh-key";
  private static final String AUTH = "test-auth-key";

  private static final Instant CREATED_AT = Instant.parse("2025-01-01T10:00:00Z");

  private static final Instant LAST_USED_AT = Instant.parse("2025-01-02T10:00:00Z");

  @BeforeEach
  void setUp() {
    userId = new UserId(UUID.fromString("018f0000-0000-7000-8000-000000000001"));

    anotherUserId = new UserId(UUID.fromString("018f0000-0000-7000-8000-000000000002"));

    subscriptionUuid = UUID.fromString("018f0000-0000-7000-8000-000000000003");

    existingSubscriptionUuid = UUID.fromString("018f0000-0000-7000-8000-000000000004");
  }

  // ---------------------------------------------------------------------------
  // register
  // ---------------------------------------------------------------------------

  @Test
  void register_shouldCreateNewSubscriptionWhenNotFound() {
    when(subscriptionRepository.findByEndpoint(ENDPOINT)).thenReturn(Optional.empty());

    when(idGenerator.generate()).thenReturn(subscriptionUuid);

    when(subscriptionRepository.save(any(WebPushSubscription.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    WebPushSubscriptionResult result = service.register(userId, ENDPOINT, P256DH, AUTH);

    ArgumentCaptor<WebPushSubscription> captor = ArgumentCaptor.forClass(WebPushSubscription.class);

    verify(subscriptionRepository).save(captor.capture());

    WebPushSubscription saved = captor.getValue();

    assertThat(saved.getId().value()).isEqualTo(subscriptionUuid);
    assertThat(saved.getUserId()).isEqualTo(userId);
    assertThat(saved.getEndpoint()).isEqualTo(ENDPOINT);

    assertThat(result.id()).isEqualTo(subscriptionUuid);
    assertThat(result.endpoint()).isEqualTo(ENDPOINT);
    assertThat(result.createdAt()).isEqualTo(saved.getCreatedAt());
    assertThat(result.lastUsedAt()).isEqualTo(saved.getLastUsedAt());

    verify(subscriptionRepository).findByEndpoint(ENDPOINT);
    verify(idGenerator).generate();
    verifyNoMoreInteractions(subscriptionRepository, idGenerator);
  }

  @Test
  void register_shouldUpdateExistingSubscription() {
    WebPushSubscription existing =
        createSubscription(
            anotherUserId,
            existingSubscriptionUuid,
            ENDPOINT,
            "old-p256dh",
            "old-auth",
            CREATED_AT,
            LAST_USED_AT);

    when(subscriptionRepository.findByEndpoint(ENDPOINT)).thenReturn(Optional.of(existing));

    when(subscriptionRepository.save(any(WebPushSubscription.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    WebPushSubscriptionResult result = service.register(userId, ENDPOINT, P256DH, AUTH);

    ArgumentCaptor<WebPushSubscription> captor = ArgumentCaptor.forClass(WebPushSubscription.class);

    verify(subscriptionRepository).save(captor.capture());

    WebPushSubscription saved = captor.getValue();

    assertThat(saved.getId()).isEqualTo(existing.getId());
    assertThat(saved.getUserId()).isEqualTo(userId);
    assertThat(saved.getEndpoint()).isEqualTo(ENDPOINT);

    assertThat(saved.getCreatedAt()).isEqualTo(CREATED_AT);
    assertThat(saved.getLastUsedAt()).isEqualTo(LAST_USED_AT);

    assertThat(result.id()).isEqualTo(existingSubscriptionUuid);
    assertThat(result.endpoint()).isEqualTo(ENDPOINT);
    assertThat(result.createdAt()).isEqualTo(CREATED_AT);
    assertThat(result.lastUsedAt()).isEqualTo(LAST_USED_AT);

    verify(subscriptionRepository).findByEndpoint(ENDPOINT);
    verify(idGenerator, never()).generate();
    verifyNoMoreInteractions(subscriptionRepository, idGenerator);
  }

  @Test
  void register_shouldKeepSameUserWhenUpdatingExistingSubscription() {
    WebPushSubscription existing =
        createSubscription(
            userId,
            existingSubscriptionUuid,
            ENDPOINT,
            "old-p256dh",
            "old-auth",
            CREATED_AT,
            LAST_USED_AT);

    when(subscriptionRepository.findByEndpoint(ENDPOINT)).thenReturn(Optional.of(existing));

    when(subscriptionRepository.save(any(WebPushSubscription.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    service.register(userId, ENDPOINT, P256DH, AUTH);

    ArgumentCaptor<WebPushSubscription> captor = ArgumentCaptor.forClass(WebPushSubscription.class);

    verify(subscriptionRepository).save(captor.capture());

    WebPushSubscription saved = captor.getValue();

    assertThat(saved.getUserId()).isEqualTo(userId);
    assertThat(saved.getId().value()).isEqualTo(existingSubscriptionUuid);

    verify(idGenerator, never()).generate();
  }

  @Test
  void register_shouldThrowExceptionWhenUserIdIsNull() {
    assertThrows(NullPointerException.class, () -> service.register(null, ENDPOINT, P256DH, AUTH));

    verifyNoInteractions(subscriptionRepository, idGenerator);
  }

  @Test
  void register_shouldThrowExceptionWhenEndpointIsNull() {
    assertThrows(NullPointerException.class, () -> service.register(userId, null, P256DH, AUTH));

    verifyNoInteractions(subscriptionRepository, idGenerator);
  }

  @Test
  void register_shouldThrowExceptionWhenEndpointIsBlank() {
    assertThrows(
        IllegalArgumentException.class, () -> service.register(userId, "  ", P256DH, AUTH));

    verifyNoInteractions(subscriptionRepository, idGenerator);
  }

  @Test
  void register_shouldThrowExceptionWhenP256dhIsNull() {
    assertThrows(NullPointerException.class, () -> service.register(userId, ENDPOINT, null, AUTH));

    verifyNoInteractions(subscriptionRepository, idGenerator);
  }

  @Test
  void register_shouldThrowExceptionWhenP256dhIsBlank() {
    assertThrows(
        IllegalArgumentException.class, () -> service.register(userId, ENDPOINT, " ", AUTH));

    verifyNoInteractions(subscriptionRepository, idGenerator);
  }

  @Test
  void register_shouldThrowExceptionWhenAuthIsNull() {
    assertThrows(
        NullPointerException.class, () -> service.register(userId, ENDPOINT, P256DH, null));

    verifyNoInteractions(subscriptionRepository, idGenerator);
  }

  @Test
  void register_shouldThrowExceptionWhenAuthIsBlank() {
    assertThrows(
        IllegalArgumentException.class, () -> service.register(userId, ENDPOINT, P256DH, "  "));

    verifyNoInteractions(subscriptionRepository, idGenerator);
  }

  // ---------------------------------------------------------------------------
  // getSubscriptions
  // ---------------------------------------------------------------------------

  @Test
  void getSubscriptions_shouldReturnSubscriptions() {
    WebPushSubscription first =
        createSubscription(
            userId, subscriptionUuid, ENDPOINT, P256DH, AUTH, CREATED_AT, LAST_USED_AT);

    WebPushSubscription second =
        createSubscription(
            userId,
            existingSubscriptionUuid,
            "https://push.example.com/subscription/456",
            "another-p256dh",
            "another-auth",
            CREATED_AT,
            null);

    when(subscriptionRepository.findAllByUserId(userId)).thenReturn(List.of(first, second));

    List<WebPushSubscriptionResult> result = service.getSubscriptions(userId);

    assertThat(result).hasSize(2);

    assertThat(result.get(0).id()).isEqualTo(subscriptionUuid);
    assertThat(result.get(0).endpoint()).isEqualTo(ENDPOINT);
    assertThat(result.get(0).createdAt()).isEqualTo(CREATED_AT);
    assertThat(result.get(0).lastUsedAt()).isEqualTo(LAST_USED_AT);

    assertThat(result.get(1).id()).isEqualTo(existingSubscriptionUuid);
    assertThat(result.get(1).lastUsedAt()).isNull();

    verify(subscriptionRepository).findAllByUserId(userId);
    verifyNoMoreInteractions(subscriptionRepository);
    verifyNoInteractions(idGenerator);
  }

  @Test
  void getSubscriptions_shouldReturnEmptyListWhenNoSubscriptionsExist() {
    when(subscriptionRepository.findAllByUserId(userId)).thenReturn(List.of());

    List<WebPushSubscriptionResult> result = service.getSubscriptions(userId);

    assertThat(result).isEmpty();

    verify(subscriptionRepository).findAllByUserId(userId);
    verifyNoMoreInteractions(subscriptionRepository);
    verifyNoInteractions(idGenerator);
  }

  @Test
  void getSubscriptions_shouldThrowExceptionWhenUserIdIsNull() {
    assertThrows(NullPointerException.class, () -> service.getSubscriptions(null));

    verifyNoInteractions(subscriptionRepository, idGenerator);
  }

  // ---------------------------------------------------------------------------
  // delete
  // ---------------------------------------------------------------------------

  @Test
  void delete_shouldDeleteSubscription() {
    service.delete(userId, ENDPOINT);

    verify(subscriptionRepository).deleteByUserIdAndEndpoint(userId, ENDPOINT);

    verifyNoMoreInteractions(subscriptionRepository);
    verifyNoInteractions(idGenerator);
  }

  @Test
  void delete_shouldThrowExceptionWhenUserIdIsNull() {
    assertThrows(NullPointerException.class, () -> service.delete(null, ENDPOINT));

    verifyNoInteractions(subscriptionRepository, idGenerator);
  }

  @Test
  void delete_shouldThrowExceptionWhenEndpointIsNull() {
    assertThrows(NullPointerException.class, () -> service.delete(userId, null));

    verifyNoInteractions(subscriptionRepository, idGenerator);
  }

  @Test
  void delete_shouldThrowExceptionWhenEndpointIsBlank() {
    assertThrows(IllegalArgumentException.class, () -> service.delete(userId, "  "));

    verifyNoInteractions(subscriptionRepository, idGenerator);
  }

  // ---------------------------------------------------------------------------
  // Test helpers
  // ---------------------------------------------------------------------------

  private WebPushSubscription createSubscription(
      UserId ownerId,
      UUID id,
      String endpoint,
      String p256dh,
      String auth,
      Instant createdAt,
      Instant lastUsedAt) {

    return WebPushSubscription.reconstitute(
        new WebPushSubscriptionId(id), ownerId, endpoint, p256dh, auth, createdAt, lastUsedAt);
  }
}
