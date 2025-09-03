package com.manuelpedreira.shorturl.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.manuelpedreira.shorturl.entities.Telemetry;

public interface TelemetryRepository extends JpaRepository<Telemetry, Long> {

}
