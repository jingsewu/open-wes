package org.openwes.wes.basic.main.data.infrastructure.persistence.mapper;

import org.openwes.wes.basic.main.data.infrastructure.persistence.po.OwnerMainDataPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface OwnerMainDataPORepository extends JpaRepository<OwnerMainDataPO, Long> {

    Collection<OwnerMainDataPO> findAllByOwnerCodeInAndWarehouseCode(Collection<String> ownCodes, String warehouseCode);
}
