package com.manuelpedreira.shorturl.controllers;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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

  private static final Logger logger = LoggerFactory.getLogger(UrlController.class);

  @Autowired
  private UrlService urlService;

  @PostMapping
  public ResponseEntity<?> postUrl(@Valid @RequestBody UrlRequestDTO urlRequest) {

    Url newUrl = urlService.create(urlRequest.getUrl(), null);

    URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
        .path("/{shortCode}")
        .buildAndExpand(newUrl.getShortCode())
        .toUri();

    UrlResponseDTO resp = new UrlResponseDTO(newUrl.getShortCode(), newUrl.getOriginalUrl(), location.toString());

    return ResponseEntity.created(location).body(resp);
  }

}
