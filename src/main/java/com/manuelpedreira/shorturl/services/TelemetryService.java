package com.manuelpedreira.shorturl.services;

import com.manuelpedreira.shorturl.entities.Telemetry;
import com.manuelpedreira.shorturl.entities.Url;

public interface TelemetryService {

  public Telemetry registerVisit(Telemetry telemetry, Url url);

}
