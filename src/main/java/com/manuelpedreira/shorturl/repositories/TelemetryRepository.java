package com.manuelpedreira.shorturl.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.manuelpedreira.shorturl.entities.Telemetry;
import com.manuelpedreira.shorturl.entities.Url;

public interface TelemetryRepository extends JpaRepository<Telemetry, Long> {

  public List<Telemetry> findByUrl(Url url);

}
