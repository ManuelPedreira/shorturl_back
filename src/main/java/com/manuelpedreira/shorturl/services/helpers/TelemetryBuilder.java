package com.manuelpedreira.shorturl.services.helpers;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.manuelpedreira.shorturl.entities.Telemetry;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class TelemetryBuilder {

  @Value("${custom.bot-agent}")
  private String BOT_AGENT;

  private Pattern comparer;

  public Telemetry buildTelemetry(HttpServletRequest req) {
    Telemetry telemetry = new Telemetry(getClientIp(req), req.getHeader("User-Agent"), null);
    telemetry.setIsBot(isBot(telemetry));

    return telemetry;
  }

  @PostConstruct
  public void createComparerPattern() {
    comparer = Pattern.compile("(" + BOT_AGENT + ")", Pattern.CASE_INSENSITIVE);
  }

  private Boolean isBot(Telemetry telemetry) {
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
