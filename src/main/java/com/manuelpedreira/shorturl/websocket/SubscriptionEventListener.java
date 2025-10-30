package com.manuelpedreira.shorturl.websocket;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.context.ApplicationListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageHeaderAccessor;
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
  public void onApplicationEvent(SessionSubscribeEvent event) {
    Message<byte[]> message = event.getMessage();
    MessageHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, SimpMessageHeaderAccessor.class);
    if (accessor == null)
      return;

    String destination = (String) accessor.getHeader("simpDestination");
    String sessionId = (String) accessor.getHeader("simpSessionId");

    if (destination == null || sessionId == null)
      return;

    Matcher m = topicPattern.matcher(destination);
    if (!m.matches())
      return;

    String shortCode = m.group(1);

    Object buffered = messageBuffer.getAndRemove(shortCode);
    if (buffered == null) {
      // no hay mensaje bufferizado para ese shortCode
      return;
    }

    // Enviamos solo a la sesión que se ha suscrito.
    // Construimos headers con el session id para que el broker entregue solo a esa
    // sesión.
    SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
    headerAccessor.setSessionId(sessionId);
    headerAccessor.setLeaveMutable(true);
    Map<String, Object> headers = headerAccessor.getMessageHeaders();

    try {
      // convertAndSend con headers que contienen sessionId rutea al cliente
      // específico
      messagingTemplate.convertAndSend(destination, buffered, headers);
    } catch (Exception e) {
      // En caso de fallo, reinyectar en buffer (opcional) o log
      // messageBuffer.put(shortCode, buffered);
      // O simplemente lo descartamos
      e.printStackTrace();
    }
  }
}
