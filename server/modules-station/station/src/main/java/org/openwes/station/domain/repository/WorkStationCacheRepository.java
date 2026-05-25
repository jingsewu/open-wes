package org.openwes.station.domain.repository;

import org.openwes.station.domain.entity.WorkStationCache;

import java.util.Collection;
import java.util.List;

public interface WorkStationCacheRepository {

    WorkStationCache findById(Long id);

    void save(WorkStationCache workStationCache);

    void delete(WorkStationCache workStationCache);

    List<WorkStationCache> findAllById(Collection<Long> workStationIds);

    void deleteById(Long workStationId);
}
