package org.openwes.wes.printer.infrastructure.persistence.mapper;

import org.openwes.wes.printer.infrastructure.persistence.po.PrintConfigPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrintConfigPORepository extends JpaRepository<PrintConfigPO, Long> {

    List<PrintConfigPO> findAllByWorkStationId(Long workStationId);
}
