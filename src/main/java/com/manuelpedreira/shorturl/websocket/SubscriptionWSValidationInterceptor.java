package com.manuelpedreira.shorturl.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SubscriptionWSValidationInterceptor implements ChannelInterceptor {

  private final Pattern topicPattern;
  private static final Logger logger = LoggerFactory.getLogger(SubscriptionWSValidationInterceptor.class);

  public SubscriptionWSValidationInterceptor(@Value("${custom.url.validation.pattern}") String pattern) {
    topicPattern = Pattern.compile("^/topic/url\\.(" + pattern + ")$");
  }

  @Override
  public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {

    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
    if (accessor == null) {
      return message;
    }

    if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
      String destination = accessor.getDestination(); // ej. /topic/url.{code}
      String sessionId = accessor.getSessionId();
      if (destination == null || sessionId == null) {
        return null;
      }

      Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
      if (sessionAttrs == null)
        return null;
      Object JWTshortCode = sessionAttrs.get("JWTshortCode");

      if (!(JWTshortCode instanceof String))
        return null;
      String shortCodeFromSession = (String) JWTshortCode;

      Matcher matcher = topicPattern.matcher(destination);
      if (matcher.matches()) {
        String shortCodeFromDestination = matcher.group(1);
        if (shortCodeFromSession == null || !shortCodeFromSession.equals(shortCodeFromDestination)) {
          logger.warn("Subscription rejected. sessionId=%s destination=%s shortCodeSession=%s%n",
              sessionId, destination, shortCodeFromSession);
          return null;
        }
      }
    }

    return message;
  }
}
