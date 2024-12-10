package org.openwes.wes.basic.warehouse.infrastructure.persistence.mapper;

import org.openwes.wes.basic.warehouse.infrastructure.persistence.po.AislePO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AislePORepository extends JpaRepository<AislePO, Long> {
}
