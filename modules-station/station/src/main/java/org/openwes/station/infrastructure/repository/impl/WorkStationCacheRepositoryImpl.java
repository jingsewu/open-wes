package org.openwes.station.infrastructure.repository.impl;

import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.domain.transfer.WorkStationCacheTransfer;
import org.openwes.station.infrastructure.persistence.mapper.WorkStationCachePORepository;
import org.openwes.station.infrastructure.persistence.po.WorkStationCachePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkStationCacheRepositoryImpl<T extends WorkStationCache, S extends WorkStationCachePO>
        implements WorkStationCacheRepository<T> {

    private final WorkStationCachePORepository<S> workStationCachePORepository;
    private final WorkStationCacheTransfer workStationCacheTransfer;

    @Override
    public T findById(Long id) {
        S s = workStationCachePORepository.findById(id).orElse(null);
        if (s == null) return null;
        return workStationCacheTransfer.toGenericDO(s);
    }

    @Override
    public void save(T workStationCache) {
        S s = workStationCacheTransfer.toGenericPO(workStationCache);
        workStationCachePORepository.save(s);
    }

    @Override
    public void delete(T workStationCache) {
        S s = workStationCacheTransfer.toGenericPO(workStationCache);
        workStationCachePORepository.delete(s);
    }

    @Override
    public List<T> findAllById(Collection<Long> workStationIds) {
        Iterable<S> ss = workStationCachePORepository.findAllById(workStationIds);
        return workStationCacheTransfer.toGenericDOs(ss);
    }
}
