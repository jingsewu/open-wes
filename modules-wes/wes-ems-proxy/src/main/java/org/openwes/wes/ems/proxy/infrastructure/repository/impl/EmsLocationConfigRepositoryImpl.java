package org.openwes.wes.ems.proxy.infrastructure.repository.impl;

import org.openwes.wes.ems.proxy.domain.entity.EmsLocationConfig;
import org.openwes.wes.ems.proxy.domain.repository.EmsLocationConfigRepository;
import org.openwes.wes.ems.proxy.infrastructure.persistence.mapper.EmsLocationConfigPORepository;
import org.openwes.wes.ems.proxy.infrastructure.persistence.transfer.EmsLocationConfigPOTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmsLocationConfigRepositoryImpl implements EmsLocationConfigRepository {

    private final EmsLocationConfigPORepository emsLocationConfigPORepository;
    private final EmsLocationConfigPOTransfer emsLocationConfigPOTransfer;

    @Override
    public void save(EmsLocationConfig emsLocationConfig) {
        emsLocationConfigPORepository.save(emsLocationConfigPOTransfer.toPO(emsLocationConfig));
    }

    @Override
    public EmsLocationConfig findByLocationCode(String locationCode) {
        return emsLocationConfigPOTransfer.toDO(emsLocationConfigPORepository.findByLocationCode(locationCode));
    }
}
