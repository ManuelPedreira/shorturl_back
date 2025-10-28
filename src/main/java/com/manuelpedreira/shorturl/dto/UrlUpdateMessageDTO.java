package com.manuelpedreira.shorturl.dto;

public class UrlUpdateMessageDTO {
  private String shortCode;
  private String originalUrl;
  private String title;
  private String description;
  private String imageUrl;
  private String status;

  public UrlUpdateMessageDTO() {
  }

  public UrlUpdateMessageDTO(String shortCode, String originalUrl, String title, String description, String imageUrl,
      String status) {
    this.shortCode = shortCode;
    this.originalUrl = originalUrl;
    this.title = title;
    this.description = description;
    this.imageUrl = imageUrl;
    this.status = status;
  }

  // getters y setters
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
