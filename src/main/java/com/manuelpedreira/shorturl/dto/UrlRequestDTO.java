package com.manuelpedreira.shorturl.dto;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotBlank;

public class UrlRequestDTO {

  @NotBlank
  @URL(protocol = "http", message = "URL must include http or https")
  private String url;

  public UrlRequestDTO() {
  }

  public UrlRequestDTO(String url) {
    this.url = url;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
