package com.manuelpedreira.shorturl.websocket;

import java.time.Instant;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class InMemoryMessageBuffer {

  private static class Buffered {
    final Object payload;
    final ScheduledFuture<?> expiryTask;
    final Instant expiryAt;

    Buffered(Object payload, ScheduledFuture<?> expiryTask, Instant expiryAt) {
      this.payload = payload;
      this.expiryTask = expiryTask;
      this.expiryAt = expiryAt;
    }
  }

  private final ConcurrentHashMap<String, Buffered> map = new ConcurrentHashMap<>();
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

  // TTL in seconds (ajústalo)
  private final long ttlSeconds = 15L;

  public void put(String shortCode, Object payload) {
    // if an existing buffered entry exists, cancel its expiry
    Buffered previous = map.remove(shortCode);
    if (previous != null && previous.expiryTask != null) {
      previous.expiryTask.cancel(false);
    }

    Instant expiryAt = Instant.now().plusSeconds(ttlSeconds);
    ScheduledFuture<?> task = scheduler.schedule(() -> {
      map.remove(shortCode);
    }, ttlSeconds, TimeUnit.SECONDS);


    map.put(shortCode, new Buffered(payload, task, expiryAt));
    System.out.println("PUT :" + shortCode + ", map: " + map.entrySet().stream()
        .map(e -> e.getKey() + "=" + e.getValue().expiryAt)
        .collect(Collectors.joining(", ", "{", "}")));
  }

  public Object getAndRemove(String shortCode) {
    Buffered b = map.remove(shortCode);
    if (b == null)
      return null;
    if (b.expiryTask != null)
      b.expiryTask.cancel(false);

    System.out.println("GET and REMOVE :" + shortCode + ", map: " + map.entrySet().stream()
        .map(e -> e.getKey() + "=" + e.getValue().expiryAt)
        .collect(Collectors.joining(", ", "{", "}")));
        
    return b.payload;
  }

  public Object peek(String shortCode) {
    Buffered b = map.get(shortCode);
    return b == null ? null : b.payload;
  }

  public void remove(String shortCode) {
    Buffered b = map.remove(shortCode);
    if (b != null && b.expiryTask != null)
      b.expiryTask.cancel(false);
  }
}
