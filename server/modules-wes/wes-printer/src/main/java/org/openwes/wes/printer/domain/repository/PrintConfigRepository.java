package org.openwes.wes.printer.domain.repository;

import org.openwes.wes.printer.domain.entity.PrintConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrintConfigRepository extends JpaRepository<PrintConfig, Long> {

    List<PrintConfig> findAllByWorkStationId(Long workStationId);
}
