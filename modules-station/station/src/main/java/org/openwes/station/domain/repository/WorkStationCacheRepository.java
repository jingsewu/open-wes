package org.openwes.station.domain.repository;

import org.openwes.station.domain.entity.WorkStationCache;

import java.util.Collection;
import java.util.List;

public interface WorkStationCacheRepository<T extends WorkStationCache> {

    T findById(Long id);

    void save(T workStationCache);

    void delete(T workStationCache);

    List<T> findAllById(Collection<Long> workStationIds);
}
