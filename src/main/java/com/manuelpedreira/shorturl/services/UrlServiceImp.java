package com.manuelpedreira.shorturl.services;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manuelpedreira.shorturl.entities.Url;
import com.manuelpedreira.shorturl.entities.User;
import com.manuelpedreira.shorturl.repositories.UrlRepository;

@Service
public class UrlServiceImp implements UrlService {

  private UrlRepository urlRepository;
  private MetadataExtractorService metadataExtractorService;
  private ShortCodeGeneratorService codeGeneratorService;

  final int MAX_ATTEMPTS = 6;

  private static final Logger logger = LoggerFactory.getLogger(UrlServiceImp.class);

  @Value("${custom.url.default.expiration.months}")
  private int defaultExpirationMonths;

  public UrlServiceImp(UrlRepository urlRepository, MetadataExtractorService metadataExtractorService,
      ShortCodeGeneratorService codeGeneratorService) {
    this.urlRepository = urlRepository;
    this.metadataExtractorService = metadataExtractorService;
    this.codeGeneratorService = codeGeneratorService;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Url> findByShortCode(String shortCode) {
    return urlRepository.findByShortCode(shortCode);
  }

  @Override
  @Transactional
  public Url create(String originalUrl, User user) {

    Url url = new Url();
    url.setOriginalUrl(originalUrl);
    url.setExpirationDate(java.time.ZonedDateTime.now(ZoneOffset.UTC)
        .plusMonths(defaultExpirationMonths)
        .toInstant());

    for (int saveAttempt = 1; saveAttempt <= MAX_ATTEMPTS; saveAttempt++) {
      try {
        url.setShortCode(codeGeneratorService.generarShortCode());
        Url urlCreated = urlRepository.save(url);

        metadataExtractorService.enrichUrlAndSaveAsync(urlCreated.getId(), originalUrl);

        return urlCreated;

      } catch (DataIntegrityViolationException dive) {
        logger.warn("ShortCode collision (attempt {}), regenerating...", saveAttempt);
        if (saveAttempt == MAX_ATTEMPTS) {
          logger.error("Max attempts reached generating unique shortCode");
          throw dive;
        }
      }
    }
    throw new IllegalStateException("Unable to create unique short code after retries");
  }

  @Override
  @Transactional
  public Url update(Url url) {
    return urlRepository.save(url);
  }

  @Override
  @Transactional
  public void delete(Url url) {
    urlRepository.delete(url);
  }
}
