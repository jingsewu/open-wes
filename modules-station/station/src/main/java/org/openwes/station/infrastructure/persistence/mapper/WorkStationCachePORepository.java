package org.openwes.station.infrastructure.persistence.mapper;

import org.openwes.station.infrastructure.persistence.po.WorkStationCachePO;
import org.springframework.data.repository.CrudRepository;

public interface WorkStationCachePORepository<T extends WorkStationCachePO> extends CrudRepository<T, Long> {
}
