package com.manuelpedreira.shorturl.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.manuelpedreira.shorturl.entities.Url;
import com.manuelpedreira.shorturl.repositories.UrlRepository;

@Service
public class UrlServiceImp implements UrlService{

  @Autowired
  private UrlRepository urlRepository;

  @Override
  public Optional<Url> findByShortCode(String shortCode) {
    return urlRepository.findByShortCode(shortCode);
  }

}
