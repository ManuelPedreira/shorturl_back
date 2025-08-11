package com.manuelpedreira.shorturl.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.manuelpedreira.shorturl.entities.Url;
import com.manuelpedreira.shorturl.services.UrlService;

@Controller
public class UrlController {

  @Autowired
  private UrlService urlService;

  @GetMapping("/{shortCode}")
  public String getURL(@PathVariable String shortCode, Model model) {
    Optional<Url> urlOptional = urlService.findByShortCode(shortCode);

    if (!urlOptional.isPresent())
      return "error/404";

    model.addAttribute("url", urlOptional.get());
    return "botPage";
  }
}
