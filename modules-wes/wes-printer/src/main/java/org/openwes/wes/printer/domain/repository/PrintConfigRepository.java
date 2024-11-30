package org.openwes.wes.printer.domain.repository;

import org.openwes.wes.printer.domain.entity.PrintConfig;

import java.util.List;

public interface PrintConfigRepository {

    List<PrintConfig> findAllByWorkStationId(Long workStationId);
}
