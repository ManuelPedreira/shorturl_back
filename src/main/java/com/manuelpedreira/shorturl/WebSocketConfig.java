package com.manuelpedreira.shorturl;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.lang.NonNull;

import com.manuelpedreira.shorturl.websocket.CookieWSHandshakeInterceptor;
import com.manuelpedreira.shorturl.websocket.SubscriptionWSValidationInterceptor;

@Configuration
@EnableWebSocketMessageBroker
@EnableScheduling
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final CookieWSHandshakeInterceptor cookieWSHandshakeInterceptor;
  private final SubscriptionWSValidationInterceptor subscriptionWSValidationInterceptor;

  public WebSocketConfig(CookieWSHandshakeInterceptor cookieWSHandshakeInterceptor,
      SubscriptionWSValidationInterceptor subscriptionWSValidationInterceptor) {
    this.cookieWSHandshakeInterceptor = cookieWSHandshakeInterceptor;
    this.subscriptionWSValidationInterceptor = subscriptionWSValidationInterceptor;
  }

  @Override
  public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic");
    registry.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
    registry.addEndpoint("/ws")
        .addInterceptors(cookieWSHandshakeInterceptor)
        .setAllowedOriginPatterns("*")
        .withSockJS();
  }

  @Override
  public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
    registration.interceptors(subscriptionWSValidationInterceptor);
  }

}