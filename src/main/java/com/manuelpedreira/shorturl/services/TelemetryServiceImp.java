package com.manuelpedreira.shorturl.services;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manuelpedreira.shorturl.entities.Telemetry;
import com.manuelpedreira.shorturl.entities.Url;
import com.manuelpedreira.shorturl.repositories.TelemetryRepository;
import com.manuelpedreira.shorturl.repositories.UrlRepository;

@Service
public class TelemetryServiceImp implements TelemetryService {

  TelemetryRepository telemetryRepository;
  UrlRepository urlRepository;

  public TelemetryServiceImp(TelemetryRepository telemetryRepository, UrlRepository urlRepository) {
    this.telemetryRepository = telemetryRepository;
    this.urlRepository = urlRepository;
  }

  @Override
  @Transactional
  public Telemetry registerVisit(Telemetry telemetry, Url url) {
    telemetry.setUrl(url);
    return telemetryRepository.save(telemetry);
  }

  @Override
  @Transactional
  @Async("telemetryExecutor")
  public void registerAsyncVisit(Telemetry telemetry, Url url) {
    Url attached = urlRepository.getReferenceById(url.getId());
    telemetry.setUrl(attached);
    telemetryRepository.save(telemetry);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Telemetry> getTelemetryByUrl(Url url) {
    return telemetryRepository.findByUrl(url);
  }

}
