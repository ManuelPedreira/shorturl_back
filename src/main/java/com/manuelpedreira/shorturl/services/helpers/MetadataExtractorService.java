package com.manuelpedreira.shorturl.services.helpers;

import java.io.IOException;
import java.text.Normalizer;
import java.time.Instant;

import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.manuelpedreira.shorturl.dto.UrlWebSocketMessageDTO;
import com.manuelpedreira.shorturl.entities.Url;
import com.manuelpedreira.shorturl.repositories.UrlRepository;
import com.manuelpedreira.shorturl.websocket.InMemoryMessageBuffer;

import jakarta.transaction.Transactional;

@Service
public class MetadataExtractorService {

  private static final Logger logger = LoggerFactory.getLogger(MetadataExtractorService.class);

  private final int maxRedirections;
  private final int ttlSeconds;

  private final UrlRepository urlRepository;
  private final SafeUrlValidator safeUrlValidator;
  private final SimpMessagingTemplate messagingTemplate;
  private final InMemoryMessageBuffer messageBuffer;

  public MetadataExtractorService(UrlRepository urlRepository, SafeUrlValidator safeUrlValidator,
      SimpMessagingTemplate messagingTemplate, InMemoryMessageBuffer messageBuffer,
      @Value("${custom.url.fetch.max_redirections}") int maxRedirections,
      @Value("${custom.websocket.expirationTime.seconds}") int ttlSeconds) {

    this.urlRepository = urlRepository;
    this.safeUrlValidator = safeUrlValidator;
    this.messagingTemplate = messagingTemplate;
    this.messageBuffer = messageBuffer;
    this.maxRedirections = maxRedirections;
    this.ttlSeconds = ttlSeconds;
  }

  @Async("metadataExecutor")
  @Transactional
  public void enrichUrlAndSaveAsync(Long urlId, String urlShortCode, String originalUrl) {
    Url url = new Url();
    url.setOriginalUrl(originalUrl);

    try {
      enrichUrlWithMetaDataJsoup(url);
      urlRepository.saveMetaData(urlId, url.getTitle(), url.getDescription(), url.getImageUrl());

      UrlWebSocketMessageDTO message = new UrlWebSocketMessageDTO(
          urlShortCode,
          originalUrl,
          url.getTitle(),
          url.getDescription(),
          url.getImageUrl(),
          "done",
          Instant.now().plusSeconds(ttlSeconds));
      // post in /topic/url.{shortCode}
      messageBuffer.put(urlShortCode, message);
      messagingTemplate.convertAndSend("/topic/url." + urlShortCode, message);

    } catch (Exception e) {
      logger.error("Failed to extract metadata for urlId=" + urlId, e);
      UrlWebSocketMessageDTO message = new UrlWebSocketMessageDTO(
          urlShortCode,
          originalUrl,
          urlShortCode,
          "",
          "",
          "error",
          Instant.now().plusSeconds(ttlSeconds));
          
      messageBuffer.put(urlShortCode, message);
      messagingTemplate.convertAndSend("/topic/url." + urlShortCode, message);
    }
  }

  public Url enrichUrlWithMetaDataJsoup(Url url) {

    try {
      Document doc = resolveRedirects(url.getOriginalUrl(), maxRedirections);

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

      url.setTitle(sanitizeText(url.getTitle(), 200));
      url.setDescription(sanitizeText(url.getDescription(), 1000));

      if (!safeUrlValidator.isSafeUrl(url.getImageUrl()))
        url.setImageUrl("");

    } catch (IOException e) {
      url.setTitle(url.getOriginalUrl());
      logger.error("Failed to fetch metadata for URL: " + url.getOriginalUrl(), e);
    }

    return url;
  }

  private Document resolveRedirects(String url, int maxRedirections) throws IOException {

    if (!safeUrlValidator.isSafeUrl(url))
      throw new IOException("URL Redirection failed");

    Response response = Jsoup.connect(url)
        .timeout(5000)
        .followRedirects(false)
        .execute();

    if (maxRedirections == 0 ||
        response.statusCode() < 300 || response.statusCode() >= 400 ||
        response.header("Location") == null)
      return response.parse();

    return resolveRedirects(response.header("Location"), maxRedirections - 1);
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

  private String sanitizeText(String input, int maxLen) {
    if (input == null)
      return "";
    // elimina cualquier HTML, deja texto y escapa entities
    String cleaned = Jsoup.clean(input, Safelist.none());
    // normaliza unicode (evita trucos con combining chars)
    cleaned = Normalizer.normalize(cleaned, Normalizer.Form.NFKC);
    // quita caracteres de control (excepto los útiles)
    cleaned = cleaned.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]+", "");
    cleaned = cleaned.trim();
    if (cleaned.length() > maxLen) {
      cleaned = cleaned.substring(0, maxLen);
    }
    return cleaned;
  }

}
