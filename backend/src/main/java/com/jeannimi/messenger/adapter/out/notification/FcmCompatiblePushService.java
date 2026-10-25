package com.jeannimi.messenger.adapter.out.notification;

import java.io.IOException;
import java.security.GeneralSecurityException;
import nl.martijndwars.webpush.Encoding;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;
import org.jose4j.lang.JoseException;

public class FcmCompatiblePushService extends PushService {

  public FcmCompatiblePushService(String publicKey, String privateKey, String subject)
      throws GeneralSecurityException {
    super(publicKey, privateKey, subject);
  }

  @Override
  public HttpPost preparePost(Notification notification, Encoding encoding)
      throws GeneralSecurityException, IOException, JoseException {

    HttpPost request = super.preparePost(notification, encoding);

    Header cryptoKey = request.getFirstHeader("Crypto-Key");

    if (cryptoKey != null) {
      String value = cryptoKey.getValue();

      String normalizedValue = value.replaceAll("=+$", "");

      if (!value.equals(normalizedValue)) {
        request.setHeader(new BasicHeader("Crypto-Key", normalizedValue));
      }
    }

    return request;
  }
}
