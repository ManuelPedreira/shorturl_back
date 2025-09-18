package com.manuelpedreira.shorturl.services;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.manuelpedreira.shorturl.entities.Url;
import com.manuelpedreira.shorturl.repositories.UrlRepository;

import jakarta.transaction.Transactional;

@Service
public class MetadataExtractorService {

  private static final Logger logger = LoggerFactory.getLogger(MetadataExtractorService.class);

  @Autowired
  UrlRepository urlRepository;

  @Async("metadataExecutor")
  @Transactional
  public void enrichUrlAndSaveAsync(Long urlId, String originalUrl) {
    Url url = new Url();
    url.setOriginalUrl(originalUrl);
    enrichUrlWithMetaDataJsoup(url);
    urlRepository.saveMetaData(urlId, url.getTitle(), url.getDescription(), url.getImageUrl());
  }

  public Url enrichUrlWithMetaDataJsoup(Url url) {

    Document doc;
    try {
      doc = Jsoup.connect(url.getOriginalUrl())
          .timeout(5000)
          .get();

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

    } catch (IOException e) {
      url.setTitle(url.getOriginalUrl());
      logger.error("Failed to fetch metadata for URL: " + url.getOriginalUrl(), e);
    }

    return url;
  }

  private String getFirstJsoupSelect(Document doc, String... metas) {

    for (String meta : metas) {
      String data = doc.select(meta).attr("content");
      if (data != null && !data.isEmpty()) {
        return data;
      }
    }
    return "";
  }

}
