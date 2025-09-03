package com.manuelpedreira.shorturl.services;

import java.util.Optional;

import com.manuelpedreira.shorturl.entities.Url;

public interface UrlService {

  Optional<Url> findByShortCode(String shortCode);

  Url save(Url newUrl);

  Boolean existsByShortCode (String shortCode);
}
