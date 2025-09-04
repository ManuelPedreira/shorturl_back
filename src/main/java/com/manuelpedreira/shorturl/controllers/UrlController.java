package com.manuelpedreira.shorturl.controllers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.manuelpedreira.shorturl.dto.UrlRequestDTO;
import com.manuelpedreira.shorturl.entities.Url;
import com.manuelpedreira.shorturl.services.UrlService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class UrlController {

  private static final Logger logger = LoggerFactory.getLogger(UrlGetController.class);

  @Autowired
  private UrlService urlService;


  @PostMapping("/api")
  public ResponseEntity<?> postUrl(@RequestBody UrlRequestDTO urlRequest) {

    try {
      Url newUrl = urlService.create(urlRequest.getUrl(), null);
      return ResponseEntity.status(HttpStatus.CREATED).body(newUrl);

    } catch (Exception e) {
      logger.error("Error in postUrl {}: {}", urlRequest.getUrl(), e.getMessage());
      return ResponseEntity.notFound().build();
    }
  }

}
