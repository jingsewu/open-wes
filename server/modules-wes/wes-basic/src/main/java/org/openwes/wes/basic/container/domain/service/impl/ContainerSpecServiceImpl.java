package org.openwes.wes.basic.container.domain.service.impl;

import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.BasicErrorDescEnum;
import org.openwes.wes.api.basic.constants.ContainerTypeEnum;
import org.openwes.wes.basic.container.domain.entity.ContainerSpec;
import org.openwes.wes.basic.container.domain.repository.ContainerRepository;
import org.openwes.wes.basic.container.domain.repository.TransferContainerRepository;
import org.openwes.wes.basic.container.domain.service.ContainerSpecService;
import org.openwes.wes.basic.work_station.domain.repository.PutWallRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContainerSpecServiceImpl implements ContainerSpecService {

    private final ContainerRepository containerRepository;
    private final PutWallRepository putWallRepository;
    private final TransferContainerRepository transferContainerRepository;

    @Override
    public void validateDelete(ContainerSpec containerSpec) {

        if (containerSpec.getContainerType() == ContainerTypeEnum.CONTAINER || containerSpec.getContainerType() == ContainerTypeEnum.SHELF) {
            boolean result = containerRepository.existByContainerSpecCode(containerSpec.getContainerSpecCode(), containerSpec.getWarehouseCode());
            if (result) {
                throw WmsException.throwWmsException(BasicErrorDescEnum.CONTAINER_SPECIFIC_CANNOT_DELETE);
            }
        } else if (containerSpec.getContainerType() == ContainerTypeEnum.PUT_WALL) {
            boolean result = putWallRepository.existByContainerSpecCode(containerSpec.getContainerSpecCode(), containerSpec.getWarehouseCode());
            if (result) {
                throw WmsException.throwWmsException(BasicErrorDescEnum.CONTAINER_SPECIFIC_CANNOT_DELETE);
            }
        } else if (containerSpec.getContainerType() == ContainerTypeEnum.TRANSFER_CONTAINER) {
            boolean result = transferContainerRepository.existByContainerSpecCode(containerSpec.getContainerSpecCode(), containerSpec.getWarehouseCode());
            if (result) {
                throw WmsException.throwWmsException(BasicErrorDescEnum.CONTAINER_SPECIFIC_CANNOT_DELETE);
            }
        }
    }
}
