package com.manuelpedreira.shorturl.services.helpers;

import org.springframework.stereotype.Service;

@Service
public class ShortCodeGeneratorService {

  public String generarShortCode() {

    String newShortCode = java.util.UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "").substring(0, 7);

    return newShortCode;
  }
}
