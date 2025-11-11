package org.openwes.wes.ems.proxy.infrastructure.repository.impl;

import lombok.RequiredArgsConstructor;
import org.openwes.wes.ems.proxy.domain.entity.EmsLocationConfig;
import org.openwes.wes.ems.proxy.domain.repository.EmsLocationConfigRepository;
import org.openwes.wes.ems.proxy.infrastructure.persistence.mapper.EmsLocationConfigPORepository;
import org.openwes.wes.ems.proxy.infrastructure.persistence.transfer.EmsLocationConfigPOTransfer;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmsLocationConfigRepositoryImpl implements EmsLocationConfigRepository {

    private final EmsLocationConfigPORepository emsLocationConfigPORepository;
    private final EmsLocationConfigPOTransfer emsLocationConfigPOTransfer;

    @Override
    @CacheEvict(value = "emsLocationConfig", allEntries = true)
    public void save(EmsLocationConfig emsLocationConfig) {
        emsLocationConfigPORepository.save(emsLocationConfigPOTransfer.toPO(emsLocationConfig));
    }

    @Override
    @Cacheable(value = "emsLocationConfig", key = "#locationCode")
    public EmsLocationConfig findByLocationCode(String locationCode) {
        return emsLocationConfigPOTransfer.toDO(emsLocationConfigPORepository.findByLocationCode(locationCode));
    }
}
