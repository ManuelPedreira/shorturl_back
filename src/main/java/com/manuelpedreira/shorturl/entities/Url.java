package com.manuelpedreira.shorturl.entities;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "urls")
public class Url {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 7)
  private String shortCode;

  @Column(nullable = false)
  private String originalUrl;

  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  private String imageUrl;

  @CreationTimestamp
  private Instant createdAt;

  private Instant expirationDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @OneToMany(mappedBy = "url", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Telemetry> telemetries;

  public Url() {
    telemetries = new ArrayList<Telemetry>();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getShortCode() {
    return shortCode;
  }

  public void setShortCode(String shortCode) {
    this.shortCode = shortCode;
  }

  public String getOriginalUrl() {
    return originalUrl;
  }

  public void setOriginalUrl(String originalUrl) {
    this.originalUrl = originalUrl;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public Instant getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(Instant expirationDate) {
    this.expirationDate = expirationDate;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Url addTelemetry(Telemetry telemetry) {
    this.telemetries.add(telemetry);
    telemetry.setUrl(this);
    return this;
  }

  public List<Telemetry> getTelemetries() {
    return telemetries;
  }

  public void setTelemetries(List<Telemetry> telemetries) {
    this.telemetries = telemetries;
  }

  @Override
  public String toString() {
    return "Url [id=" + id + ", shortCode=" + shortCode + ", originalUrl=" + originalUrl + ", title=" + title
        + ", description=" + description + ", imageUrl=" + imageUrl + ", createdAt=" + createdAt + ", expirationDate="
        + expirationDate + ", user=" + user +" ]";
  }

}
