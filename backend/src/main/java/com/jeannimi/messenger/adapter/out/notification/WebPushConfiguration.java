package com.jeannimi.messenger.adapter.out.notification;

import java.security.GeneralSecurityException;
import java.security.Security;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class WebPushConfiguration {

  @Bean
  public PushService pushService(
      @Value("${app.web-push.vapid.public-key}") String publicKey,
      @Value("${app.web-push.vapid.private-key}") String privateKey,
      @Value("${app.web-push.vapid.subject}") String subject)
      throws GeneralSecurityException {

    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }

    log.info("Web Push service initialized");

    return new PushService(publicKey, privateKey, subject);
  }
}
