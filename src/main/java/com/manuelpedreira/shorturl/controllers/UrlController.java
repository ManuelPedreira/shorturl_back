package com.manuelpedreira.shorturl.controllers;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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

  @Transactional
  @PostMapping("/api")
  public ResponseEntity<?> postUrl(@RequestBody UrlRequestDTO urlRequest) {

    try {
      Url newUrl = getUrlDataWithJsoup(urlRequest.getUrl());
      urlService.save(newUrl);

      System.err.println(newUrl);

      return ResponseEntity.status(HttpStatus.CREATED).body(newUrl);

    } catch (Exception e) {
      logger.error("Error extracting metadata from {}: {}", urlRequest.getUrl(), e.getMessage());
      return ResponseEntity.notFound().build();
    }
  }

  private Url getUrlDataWithJsoup(String urlPage) throws IOException {

    Url url = new Url();
    url.setOriginalUrl(urlPage);

    Document doc = Jsoup.connect(urlPage).get();

    url.setTitle(doc.title());
    if (url.getTitle().isEmpty())
      url.setTitle(getFirstJsoupSelect(doc,
          "meta[property=og:title]",
          "meta[name=twitter:title]"));

    url.setDescription(getFirstJsoupSelect(doc,
        "meta[name=description]",
        "meta[property=og:description]",
        "meta[name=twitter:description]"));

    url.setImageUrl(getFirstJsoupSelect(doc,
        "meta[property=og:image]",
        "meta[property=og:image:url]",
        "meta[name=twitter:image]",
        "meta[name=image]"));

    return url;
  }

  private String getFirstJsoupSelect(Document doc, String... metas) {
    Integer count = 0;

    for (String meta : metas) {
      String data = doc.select(meta).attr("content");
      count++;
      if (data != null && !data.isEmpty()) {
        logger.info("found at try " + count + " ! ->" + meta + " -> " + data);
        return data;
      }
    }
    return "";
  }
}
