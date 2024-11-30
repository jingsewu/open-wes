package org.openwes.wes.basic.main.data.domain.repository;

import org.openwes.wes.basic.main.data.domain.entity.OwnerMainData;

import java.util.Collection;

public interface OwnerMainDataRepository {

    void save(OwnerMainData ownerMainData);

    OwnerMainData findById(Long id);

    Collection<OwnerMainData> findAllByOwnerCodes(Collection<String> ownCodes, String warehouseCode);
}
