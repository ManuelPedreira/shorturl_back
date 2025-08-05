package com.manuelpedreira.shorturl.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.manuelpedreira.shorturl.entities.Url;
import com.manuelpedreira.shorturl.services.UrlService;

@RestController
public class UrlController {

  @Autowired
  private UrlService urlService;

  @GetMapping("/{shortCode}")
  public ResponseEntity<?> getURL(@PathVariable String shortCode) {
    Optional<Url> urlOptional = urlService.findByShortCode(shortCode);

    if (!urlOptional.isPresent()) 
      return ResponseEntity.notFound().build();
    
    
    return ResponseEntity.status(HttpStatus.OK).body(urlOptional.orElseThrow());
  }
}
