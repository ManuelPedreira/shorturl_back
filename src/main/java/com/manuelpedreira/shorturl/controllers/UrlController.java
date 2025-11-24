package com.manuelpedreira.shorturl.controllers;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.manuelpedreira.shorturl.dto.UrlRequestDTO;
import com.manuelpedreira.shorturl.dto.UrlResponseDTO;
import com.manuelpedreira.shorturl.entities.Url;
import com.manuelpedreira.shorturl.services.UrlService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api")
@Validated
public class UrlController {

  private final UrlService urlService;
  private final String publicHost;

  public UrlController(UrlService urlService, @Value("${server.public.host}") String publicHost) {
    this.urlService = urlService;
    this.publicHost = publicHost;
  }

  @PostMapping
  public ResponseEntity<?> postUrl(@Valid @RequestBody UrlRequestDTO urlRequest) {

    Url newUrl = urlService.create(urlRequest.getUrl(), null);

    UriComponentsBuilder uriComponentsBuilder = (StringUtils.hasText(publicHost)
        ? ServletUriComponentsBuilder.fromUriString(publicHost)
        : ServletUriComponentsBuilder.fromCurrentContextPath());

    URI location = uriComponentsBuilder
        .path("/{shortCode}")
        .buildAndExpand(newUrl.getShortCode())
        .toUri();

    UrlResponseDTO resp = new UrlResponseDTO(newUrl.getShortCode(), newUrl.getOriginalUrl(), location.toString());

    return ResponseEntity.created(location).body(resp);
  }
}
