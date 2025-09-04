package com.manuelpedreira.shorturl.services;

import java.io.IOException;
import java.util.Optional;

import com.manuelpedreira.shorturl.entities.Url;
import com.manuelpedreira.shorturl.entities.User;

public interface UrlService {

  Optional<Url> findByShortCode(String shortCode);

  Url create(String originalUrl, User user)  throws IOException ;

  Url update(Url newUrl);
}
