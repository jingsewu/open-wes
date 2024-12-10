package org.openwes.wes.task.domain.service.impl;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.OperationTaskErrorDescEnum;
import org.openwes.wes.api.task.constants.TransferContainerStatusEnum;
import org.openwes.wes.task.domain.entity.TransferContainer;
import org.openwes.wes.task.domain.repository.TransferContainerRepository;
import org.openwes.wes.task.domain.service.TransferContainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransferContainerServiceImpl implements TransferContainerService {

    private final TransferContainerRepository transferContainerRepository;

    @Override
    public void validateBindContainer(TransferContainer transferContainer) {
        if (transferContainer != null && TransferContainerStatusEnum.IDLE != transferContainer.getTransferContainerStatus()) {
            throw WmsException.throwWmsException(OperationTaskErrorDescEnum.UNAVAILABLE_TRANSFER_CONTAINER,
                    transferContainer.getTransferContainerCode());
        }
    }
}
