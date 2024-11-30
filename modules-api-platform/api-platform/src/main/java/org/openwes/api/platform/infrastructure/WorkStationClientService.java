package org.openwes.api.platform.infrastructure;


import org.openwes.api.platform.application.service.handler.request.station.TagPtlRequestHandler;

public interface WorkStationClientService {

    void tapPtl(TagPtlRequestHandler.TapPtlEvent tapPtlEvent);
}
