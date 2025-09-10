package com.manuelpedreira.shorturl.dto;

public class UrlResponseDTO {

  private String shortCode;
  private String originalUrl;
  private String shortUrl;

  public UrlResponseDTO() {
  }

  public UrlResponseDTO(String shortCode, String originalUrl, String shortUrl) {
    this.shortCode = shortCode;
    this.originalUrl = originalUrl;
    this.shortUrl = shortUrl;
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

  public String getShortUrl() {
    return shortUrl;
  }

  public void setShortUrl(String shortUrl) {
    this.shortUrl = shortUrl;
  }

}
