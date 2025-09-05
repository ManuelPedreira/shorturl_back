package com.manuelpedreira.shorturl.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manuelpedreira.shorturl.entities.Telemetry;
import com.manuelpedreira.shorturl.entities.Url;
import com.manuelpedreira.shorturl.repositories.TelemetryRepository;

@Service
public class TelemetryServiceImp implements TelemetryService {

  @Autowired
  TelemetryRepository telemetryRepository;

  @Override
  @Transactional
  public Telemetry registerVisit(Telemetry telemetry, Url url) {
    telemetry.setUrl(url);
    return telemetryRepository.save(telemetry);
  }

}
