package com.manuelpedreira.shorturl.services.helpers;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;

@Service
public class ShortCodeGeneratorService {

  private static final char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz0123456789".toCharArray();
  private static final int DEFAULT_LENGTH = 7; // matches route pattern {shortCode:[a-zA-Z0-9]{7}}
  private final SecureRandom secureRandom = new SecureRandom();

  public String generateShortCode() {
    return generate(DEFAULT_LENGTH);
  }

  public String generate(int length) {
    if (length <= 0) throw new IllegalArgumentException("length must be > 0");
    StringBuilder builder = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      int idx = secureRandom.nextInt(ALPHABET.length);
      builder.append(ALPHABET[idx]);
    }
    return builder.toString();
  }
}
