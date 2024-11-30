package org.openwes.wes.ems.proxy.infrastructure.persistence.mapper;

import org.openwes.wes.ems.proxy.infrastructure.persistence.po.EmsLocationConfigPO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmsLocationConfigPORepository extends JpaRepository<EmsLocationConfigPO, Long> {

    EmsLocationConfigPO findByLocationCode(String locationCode);
}
