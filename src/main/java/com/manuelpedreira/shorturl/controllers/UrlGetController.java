package com.manuelpedreira.shorturl.controllers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.manuelpedreira.shorturl.entities.Telemetry;
import com.manuelpedreira.shorturl.entities.Url;
import com.manuelpedreira.shorturl.services.TelemetryService;
import com.manuelpedreira.shorturl.services.UrlService;

import com.manuelpedreira.shorturl.services.helpers.TelemetryBuilder;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class UrlGetController {

  private final UrlService urlService;
  private final TelemetryService telemetryService;
  private final TelemetryBuilder telemetryBuilder;

  public UrlGetController(UrlService urlService, TelemetryService telemetryService, TelemetryBuilder telemetryBuilder) {
    this.urlService = urlService;
    this.telemetryService = telemetryService;
    this.telemetryBuilder = telemetryBuilder;
  }

  @GetMapping("/{shortCode:[a-zA-Z0-9]{7}}")
  // http://localhost:8080/abcd123
  public ModelAndView getURL(@PathVariable String shortCode, HttpServletRequest req, Model model) {

    Optional<Url> urlOptional = urlService.findByShortCode(shortCode);

    if (urlOptional.isEmpty())
      return new ModelAndView("error/404");

    Url url = urlOptional.get();
    Telemetry telemetry = telemetryBuilder.buildTelemetry(req);

    if (telemetry.isBot()) {
      fillDefaultUrl(url);
      model.addAttribute("url", url);
      return new ModelAndView("botPage");

    } else {
      telemetryService.registerAsyncVisit(telemetry, url);

      RedirectView redirectView = new RedirectView(url.getOriginalUrl(), false);
      redirectView.setStatusCode(HttpStatus.TEMPORARY_REDIRECT);
      return new ModelAndView(redirectView);
    }
  }

  private void fillDefaultUrl(Url url) {
    if (isNullOrBlank(url.getTitle())) {
      try {
        url.setTitle(new URI(url.getOriginalUrl()).getHost());
      } catch (URISyntaxException e) {
        url.setTitle(url.getShortCode());
      }
    }

    if (isNullOrBlank(url.getDescription())) {
      url.setDescription(url.getOriginalUrl());
    }
  }

  private boolean isNullOrBlank(String evaluate) {
    return (evaluate == null || evaluate.isBlank());
  }
}
