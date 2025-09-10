package com.manuelpedreira.shorturl.services;

import java.util.List;

import com.manuelpedreira.shorturl.entities.Telemetry;
import com.manuelpedreira.shorturl.entities.Url;

public interface TelemetryService {

  public Telemetry registerVisit(Telemetry telemetry, Url url);

  public void registerAsyncVisit(Telemetry telemetry, Url url);

  public List<Telemetry> getTelemegryByUrl (Url url);

}
