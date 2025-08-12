package com.manuelpedreira.shorturl.controllers;

import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.manuelpedreira.shorturl.entities.Url;
import com.manuelpedreira.shorturl.services.UrlService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class UrlController {

  @Autowired
  private UrlService urlService;

  @Value("${custom.bot-agent}")
  private String BOT_AGENT;

  public boolean isBotAgent(String userAgent) {
    Pattern comparer = Pattern.compile("(" + BOT_AGENT + ")", Pattern.CASE_INSENSITIVE);
    return userAgent != null && comparer.matcher(userAgent).find();
  }

  public String getClientIp(HttpServletRequest req) {
    String xff = req.getHeader("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) {
      return xff.split(",")[0].trim();
    }
    return req.getRemoteAddr();
  }

  @GetMapping("/{shortCode}")
  //http://localhost:8080/abcd123
  public ModelAndView getURL(@PathVariable String shortCode, HttpServletRequest req, Model model) {

    Optional<Url> urlOptional = urlService.findByShortCode(shortCode);

    if (urlOptional.isEmpty())
      return new ModelAndView("error/404");

    String userAgent = req.getHeader("User-Agent");
    String ip = getClientIp(req);

    System.err.println("User Agent: " + userAgent);
    System.err.println("Is Bot: " + isBotAgent(userAgent));
    System.err.println("IP: " + ip);

    if (isBotAgent(userAgent)) {
      model.addAttribute("url", urlOptional.get());
      return new ModelAndView("botPage");
    } else {
      RedirectView redirectView = new RedirectView(urlOptional.get().getOriginalUrl(), true);
      redirectView.setStatusCode(HttpStatus.TEMPORARY_REDIRECT);
      return new ModelAndView(redirectView);
    }
  }
}
