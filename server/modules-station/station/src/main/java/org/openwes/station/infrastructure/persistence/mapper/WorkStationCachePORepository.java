package org.openwes.station.infrastructure.persistence.mapper;

import org.openwes.station.domain.entity.WorkStationCache;
import org.springframework.data.repository.CrudRepository;

public interface WorkStationCachePORepository extends CrudRepository<WorkStationCache, Long> {
}
