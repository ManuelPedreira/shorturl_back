package com.manuelpedreira.shorturl.entities;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "telemetry")
public class Telemetry {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "url_id")
  private Url url;

  @CreationTimestamp
  private Instant eventAt;

  @Column(length = 45)
  private String ipAddress;

  private String userAgent;

  @Column(length = 2)
  private String countryCode;

  @Transient
  private Boolean isBot;

  public Telemetry() {
  }

  public Telemetry(String ipAddress, String userAgent, String countryCode) {
    this();
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
    this.countryCode = countryCode;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Url getUrl() {
    return url;
  }

  public void setUrl(Url url) {
    this.url = url;
  }

  public Instant getEventAt() {
    return eventAt;
  }

  public void setEventAt(Instant eventAt) {
    this.eventAt = eventAt;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public Boolean isBot() {
    return isBot;
  }

  public void setIsBot(Boolean isBot) {
    this.isBot = isBot;
  }

  @Override
  public String toString() {
    return "Telemetry [id=" + id + ", eventAt=" + eventAt + ", ipAddress=" + ipAddress + ", userAgent=" + userAgent
        + ", countryCode=" + countryCode + "]";
  }

}
