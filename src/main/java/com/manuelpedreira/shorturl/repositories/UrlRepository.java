package com.manuelpedreira.shorturl.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.manuelpedreira.shorturl.entities.Url;

public interface UrlRepository extends JpaRepository<Url, Long> {

  Optional<Url> findByShortCode(String shortCode);

  Boolean existsByShortCode(String shortCode);

  @Modifying
  @Query("UPDATE Url u SET u.title = :title, u.description = :description, u.imageUrl = :imageUrl WHERE u.id = :id")
  void saveMetaData(
      @Param("id") Long id,
      @Param("title") String title,
      @Param("description") String description,
      @Param("imageUrl") String imageUrl);

}
