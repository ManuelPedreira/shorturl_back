package com.manuelpedreira.shorturl.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.manuelpedreira.shorturl.entities.Url;

public interface UrlRepository extends JpaRepository<Url, Long> {

  Optional<Url> findByShortCode(String shortCode);

  Boolean existsByShortCode(String shortCode);

}
