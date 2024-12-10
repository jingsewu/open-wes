package org.openwes.wes.ems.proxy.domain.repository;

import org.openwes.wes.ems.proxy.domain.entity.EmsLocationConfig;

public interface EmsLocationConfigRepository {

    void save(EmsLocationConfig emsLocationConfig);

    EmsLocationConfig findByLocationCode(String locationCode);
}
