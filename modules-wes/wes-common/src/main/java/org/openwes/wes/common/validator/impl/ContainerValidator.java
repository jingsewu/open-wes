package org.openwes.wes.common.validator.impl;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.BasicErrorDescEnum;
import org.openwes.wes.api.basic.IContainerApi;
import org.openwes.wes.api.basic.dto.ContainerDTO;
import org.openwes.wes.common.validator.IValidator;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContainerValidator implements IValidator<ContainerValidator.ContainerValidatorObject, Void> {

    private final IContainerApi containerApi;

    @Override
    public Void validate(ContainerValidatorObject validateObject) {
        String containerCode = validateObject.getContainerCode();
        String warehouseCode = validateObject.getWarehouseCode();
        ContainerDTO containerDTO = containerApi.queryContainer(containerCode, warehouseCode);

        if (containerDTO == null) {
            throw WmsException.throwWmsException(BasicErrorDescEnum.CONTAINER_NOT_EXIST, validateObject.getContainerCode());
        }

        if (StringUtils.isNotEmpty(validateObject.getContainerSlotCode())
                && containerDTO.getContainerSlots().stream()
                .noneMatch(v -> v.getContainerSlotCode().equals(validateObject.getContainerSlotCode()))) {
            throw WmsException.throwWmsException(BasicErrorDescEnum.CONTAINER_SLOT_NOT_EXIST, validateObject.getContainerSlotCode());
        }

        return null;
    }

    @Override
    public ValidatorName getValidatorName() {
        return ValidatorName.CONTAINER;
    }

    @Data
    @Accessors(chain = true)
    public static class ContainerValidatorObject {
        private String containerCode;
        private String containerSlotCode;
        private String warehouseCode;
    }
}
