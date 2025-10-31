package com.manuelpedreira.shorturl.websocket;

import java.time.Instant;
import java.util.concurrent.*;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.manuelpedreira.shorturl.dto.UrlWebSocketMessageDTO;

@Component
public class InMemoryMessageBuffer {

  private final ConcurrentHashMap<String, UrlWebSocketMessageDTO> map = new ConcurrentHashMap<>();

  @Scheduled(fixedRate = 5000)
  public void cleanExpiredEntries() {
    Instant now = Instant.now();
    map.entrySet().removeIf(message -> message.getValue().getExpiryAt().isBefore(now));
  }

  public void put(String shortCode, UrlWebSocketMessageDTO message) {
    map.put(shortCode, message);
  }

  public UrlWebSocketMessageDTO getAndRemove(String shortCode) {
    return map.remove(shortCode);
  }

  public UrlWebSocketMessageDTO get(String shortCode) {
    return map.get(shortCode);
  }

  public void remove(String shortCode) {
    map.remove(shortCode);
  }
}
