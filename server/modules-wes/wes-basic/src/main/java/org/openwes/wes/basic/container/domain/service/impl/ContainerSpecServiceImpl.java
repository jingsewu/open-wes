package org.openwes.wes.basic.container.domain.service.impl;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.BasicErrorDescEnum;
import org.openwes.wes.basic.container.domain.entity.ContainerSpec;
import org.openwes.wes.basic.container.domain.repository.ContainerRepository;
import org.openwes.wes.basic.container.domain.service.ContainerSpecService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContainerSpecServiceImpl implements ContainerSpecService {

    private final ContainerRepository containerRepository;

    @Override
    public void validateDelete(ContainerSpec containerSpec) {
        boolean result = containerRepository.existByContainerSpecCode(containerSpec.getContainerSpecCode(), containerSpec.getWarehouseCode());
        if (result) {
            throw WmsException.throwWmsException(BasicErrorDescEnum.CONTAINER_SPECIFIC_CANNOT_DELETE);
        }

        //todo validate put wall exits
    }
}
