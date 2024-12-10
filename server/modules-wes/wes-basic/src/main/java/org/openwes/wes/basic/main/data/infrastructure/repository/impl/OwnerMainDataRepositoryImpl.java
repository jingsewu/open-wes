package org.openwes.wes.basic.main.data.infrastructure.repository.impl;

import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.wes.basic.main.data.domain.entity.OwnerMainData;
import org.openwes.wes.basic.main.data.domain.repository.OwnerMainDataRepository;
import org.openwes.wes.basic.main.data.infrastructure.persistence.mapper.OwnerMainDataPORepository;
import org.openwes.wes.basic.main.data.infrastructure.persistence.transfer.OwnerMainDataPOTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class OwnerMainDataRepositoryImpl implements OwnerMainDataRepository {

    private final OwnerMainDataPORepository ownerMainDataPORepository;
    private final OwnerMainDataPOTransfer ownerMainDataPOTransfer;

    @Override
    @CacheEvict(cacheNames = RedisConstants.OWNER_MAIN_DATA_CACHE, key = "#ownerMainData.ownerCode + #ownerMainData.warehouseCode")
    public void save(OwnerMainData ownerMainData) {
        ownerMainDataPORepository.save(ownerMainDataPOTransfer.toPO(ownerMainData));
    }

    @Override
    public Collection<OwnerMainData> findAllByOwnerCodes(Collection<String> ownCodes, String warehouseCode) {
        return ownerMainDataPOTransfer.toDOs(ownerMainDataPORepository.findAllByOwnerCodeInAndWarehouseCode(ownCodes, warehouseCode));
    }

    @Override
    public OwnerMainData findById(Long id) {
        return ownerMainDataPORepository.findById(id).map(ownerMainDataPOTransfer::toDO).orElseThrow();
    }
}
