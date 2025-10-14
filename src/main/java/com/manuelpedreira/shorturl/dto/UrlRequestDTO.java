package com.manuelpedreira.shorturl.dto;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UrlRequestDTO {

  @NotBlank
  @URL(regexp = "https?://.*", flags = {
      jakarta.validation.constraints.Pattern.Flag.CASE_INSENSITIVE }, message = "URL must start with http:// or https://")
  @Pattern(regexp = ".*\\..*", message = "URL must contain at least one dot (.)")
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
