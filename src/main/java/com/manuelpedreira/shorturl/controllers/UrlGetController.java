package com.manuelpedreira.shorturl.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.manuelpedreira.shorturl.entities.Telemetry;
import com.manuelpedreira.shorturl.entities.Url;
import com.manuelpedreira.shorturl.services.TelemetryBuilder;
import com.manuelpedreira.shorturl.services.TelemetryService;
import com.manuelpedreira.shorturl.services.UrlService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class UrlGetController {

  @Autowired
  private UrlService urlService;

  @Autowired
  private TelemetryService telemetryService;

  @Autowired
  private TelemetryBuilder telemetryBuilder;

  @GetMapping("/{shortCode:[a-zA-Z0-9]{7}}")
  // http://localhost:8080/abcd123
  public ModelAndView getURL(@PathVariable String shortCode, HttpServletRequest req, Model model) {

    Optional<Url> urlOptional = urlService.findByShortCode(shortCode);

    if (urlOptional.isEmpty())
      return new ModelAndView("error/404");

    Url url = urlOptional.get();
    Telemetry telemetry = telemetryBuilder.buildTelemetry(req);

    if (telemetry.isBot()) {
      model.addAttribute("url", url);
      return new ModelAndView("botPage");

    } else {
      telemetryService.registerAsyncVisit(telemetry, url);

      RedirectView redirectView = new RedirectView(url.getOriginalUrl(), true);
      redirectView.setStatusCode(HttpStatus.TEMPORARY_REDIRECT);
      return new ModelAndView(redirectView);
    }
  }
}
