package com.manuelpedreira.shorturl.services.helpers;

import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ShortCodeGeneratorService {

  private final char[] ALPHABET;
  private final int DEFAULT_LENGTH;
  private final SecureRandom secureRandom = new SecureRandom();

  public ShortCodeGeneratorService(@Value("${custom.url.generation.alphabet}") String ALPHABET,
      @Value("${custom.url.generation.length}") int DEFAULT_LENGTH) {
    this.ALPHABET = ALPHABET.toCharArray();
    this.DEFAULT_LENGTH = DEFAULT_LENGTH;
  }

  public String generateShortCode() {
    return generate(DEFAULT_LENGTH);
  }

  public String generate(int length) {
    if (length <= 0)
      throw new IllegalArgumentException("length must be > 0");
    StringBuilder builder = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      int idx = secureRandom.nextInt(ALPHABET.length);
      builder.append(ALPHABET[idx]);
    }
    return builder.toString();
  }
}
