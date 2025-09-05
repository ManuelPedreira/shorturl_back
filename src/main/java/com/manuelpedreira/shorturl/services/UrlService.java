package com.manuelpedreira.shorturl.services;

import java.io.IOException;
import java.util.Optional;

import com.manuelpedreira.shorturl.entities.Url;
import com.manuelpedreira.shorturl.entities.User;

public interface UrlService {

  public Optional<Url> findByShortCode(String shortCode);

  public Url create(String originalUrl, User user) throws IOException;

  public Url update(Url newUrl);
}
