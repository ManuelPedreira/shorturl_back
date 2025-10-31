package com.manuelpedreira.shorturl.websocket;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
public class SubscriptionEventListener implements ApplicationListener<SessionSubscribeEvent> {

  private final InMemoryMessageBuffer messageBuffer;
  private final SimpMessagingTemplate messagingTemplate;

  // regex para parsear /topic/url.{code}
  private final Pattern topicPattern = Pattern.compile("^/topic/url\\.([A-Za-z0-9_-]+)$");

  public SubscriptionEventListener(InMemoryMessageBuffer messageBuffer,
      SimpMessagingTemplate messagingTemplate) {
    this.messageBuffer = messageBuffer;
    this.messagingTemplate = messagingTemplate;
  }

  @Override
  public void onApplicationEvent(@NonNull SessionSubscribeEvent event) {
    SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(event.getMessage());

    String sessionId = accessor.getSessionId();
    String destination = accessor.getDestination(); // /topic/url.{code}
    // accessor.getSubscriptionId();
    // accessor.getUser();

    if (destination == null || sessionId == null)
      return;

    Matcher matcher = topicPattern.matcher(destination);

    if (!matcher.matches())
      return;

    String shortCode = matcher.group(1);
    var message = messageBuffer.getAndRemove(shortCode);

    if (message == null)
      return;

    SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
    headerAccessor.setSessionId(sessionId);
    Map<String, Object> headers = headerAccessor.getMessageHeaders();

    messagingTemplate.convertAndSend(destination, message, headers);
  }
}
