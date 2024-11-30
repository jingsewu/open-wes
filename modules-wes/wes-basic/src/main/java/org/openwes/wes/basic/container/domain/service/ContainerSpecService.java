package org.openwes.wes.basic.container.domain.service;

import org.openwes.wes.basic.container.domain.entity.ContainerSpec;

public interface ContainerSpecService {
    void validateDelete(ContainerSpec containerSpec);
}
