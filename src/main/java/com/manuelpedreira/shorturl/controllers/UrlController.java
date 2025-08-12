package com.manuelpedreira.shorturl.controllers;

import java.util.Optional;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.manuelpedreira.shorturl.entities.Url;
import com.manuelpedreira.shorturl.services.UrlService;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;

@Controller
public class UrlController {

  private static final Logger logger = LoggerFactory.getLogger(UrlController.class);

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
  // http://localhost:8080/abcd123
  public ModelAndView getURL(@PathVariable String shortCode, HttpServletRequest req, Model model) {

    Optional<Url> urlOptional = urlService.findByShortCode(shortCode);

    if (urlOptional.isEmpty())
      return new ModelAndView("error/404");

    Url url = urlOptional.get();
    String userAgent = req.getHeader("User-Agent");
    String ip = getClientIp(req);

    logger.info("User Agent: {}", userAgent);
    logger.info("Is Bot: {}", isBotAgent(userAgent));
    logger.info("IP: {}", ip);

    if (isBotAgent(userAgent)) {
      model.addAttribute("url", url);
      return new ModelAndView("botPage");
    } else {
      RedirectView redirectView = new RedirectView(url.getOriginalUrl(), true);
      redirectView.setStatusCode(HttpStatus.TEMPORARY_REDIRECT);
      return new ModelAndView(redirectView);
    }
  }
}
