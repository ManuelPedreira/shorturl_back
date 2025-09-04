package com.manuelpedreira.shorturl.services;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.manuelpedreira.shorturl.entities.Telemetry;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class TelemetryBuilder {

  @Value("${custom.bot-agent}")
  private String BOT_AGENT;

  private static final Logger logger = LoggerFactory.getLogger(TelemetryBuilder.class);

  public Telemetry buildTelemetry(HttpServletRequest req) {
    Telemetry telemetry = new Telemetry(getClientIp(req), req.getHeader("User-Agent"), null);
    telemetry.setIsBot(isBot(telemetry));

    logger.info("User Agent: {}", telemetry.getUserAgent());
    logger.info("Is Bot: {}", telemetry.isBot());
    logger.info("IP: {}", telemetry.getIpAddress());

    return telemetry;
  }

  private Boolean isBot(Telemetry telemetry) {
    Pattern comparer = Pattern.compile("(" + BOT_AGENT + ")", Pattern.CASE_INSENSITIVE);
    return telemetry.getUserAgent() != null && comparer.matcher(telemetry.getUserAgent()).find();
  }

  private String getClientIp(HttpServletRequest req) {
    String xff = req.getHeader("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) {
      return xff.split(",")[0].trim();
    }
    return req.getRemoteAddr();
  }
}
