package org.openwes.station.infrastructure.repository.impl;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.infrastructure.persistence.mapper.WorkStationCachePORepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkStationCacheRepositoryImpl implements WorkStationCacheRepository {

    private final WorkStationCachePORepository workStationCachePORepository;

    @Override
    public WorkStationCache findById(Long id) {
        return workStationCachePORepository.findById(id).orElse(null);
    }

    @Override
    public void save(WorkStationCache workStationCache) {
        workStationCachePORepository.save(workStationCache);
    }

    @Override
    public void delete(WorkStationCache workStationCache) {
        workStationCachePORepository.delete(workStationCache);
    }

    @Override
    public List<WorkStationCache> findAllById(Collection<Long> workStationIds) {
        List<WorkStationCache> result = Lists.newArrayList();
        workStationCachePORepository.findAllById(workStationIds).forEach(result::add);
        return result;
    }

    @Override
    public void deleteById(Long workStationId) {
        workStationCachePORepository.deleteById(workStationId);
    }
}
