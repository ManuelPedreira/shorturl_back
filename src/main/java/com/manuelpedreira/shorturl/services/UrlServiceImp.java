package com.manuelpedreira.shorturl.services;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
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
  public Url create(String originalUrl, User user) throws IOException {

    Url url = new Url();

    url.setOriginalUrl(originalUrl);
    url.setShortCode(codeGeneratorService.generarShortCode());

    metadataExtractorService.enrichUrlWithMetaDataJsoup(url);

    url.setCreatedAt(java.time.LocalDateTime.now());
    url.setExpirationDate(
        java.time.LocalDateTime.now().plus(defaultExpirationMonths, java.time.temporal.ChronoUnit.MONTHS));

    return urlRepository.save(url);
  }

  @Override
  @Transactional
  public Url update(Url url) {
    return urlRepository.save(url);
  }
}
