package org.openwes.wes.common.validator.impl;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.BasicErrorDescEnum;
import org.openwes.wes.api.basic.IContainerApi;
import org.openwes.wes.api.basic.dto.ContainerDTO;
import org.openwes.wes.api.stock.IStockApi;
import org.openwes.wes.api.stock.dto.ContainerStockDTO;
import org.openwes.wes.common.validator.IValidator;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmptyContainerValidator implements IValidator<List<EmptyContainerValidator.ContainerValidatorObject>, List<ContainerDTO>> {

    private final IContainerApi containerApi;
    private final IStockApi iStockApi;


    @Override
    public List<ContainerDTO> validate(List<ContainerValidatorObject> validateObjects) {
        Map<String, List<EmptyContainerValidator.ContainerValidatorObject>> validateObjectMap
                = validateObjects.stream().collect(Collectors.groupingBy(EmptyContainerValidator.ContainerValidatorObject::getWarehouseCode));

        List<ContainerDTO> containerDTOS = new ArrayList<>();

        for (Map.Entry<String, List<ContainerValidatorObject>> entry : validateObjectMap.entrySet()) {
            String warehouseCode = entry.getKey();
            List<EmptyContainerValidator.ContainerValidatorObject> validatorObjects = entry.getValue();
            Set<String> containerCodes = validatorObjects.stream()
                    .map(EmptyContainerValidator.ContainerValidatorObject::getContainerCode)
                    .collect(Collectors.toSet());

            Collection<ContainerDTO> dtos = validate(containerCodes, warehouseCode);
            containerDTOS.addAll(dtos);
        }

        return containerDTOS;
    }

    private Collection<ContainerDTO> validate(Set<String> containerCodes, String warehouseCode) {
        Collection<ContainerDTO> containerDTOS = containerApi.queryContainer(containerCodes, warehouseCode);
        if (CollectionUtils.isEmpty(containerDTOS)) {
            return Collections.emptyList();
        }

        List<ContainerStockDTO> containerStocks = iStockApi.getContainerStocks(containerCodes, warehouseCode);

        // 任何一个格口有库存，就说明容器不为空
        if (CollectionUtils.isNotEmpty(containerStocks)) {
            throw WmsException.throwWmsException(BasicErrorDescEnum.CONTAINER_CONTAINS_STOCK);
        }

        return containerDTOS;
    }

    @Override
    public ValidatorName getValidatorName() {
        return ValidatorName.EMPTY_CONTAINER;
    }

    @Data
    @Builder
    @Accessors(chain = true)
    public static class ContainerValidatorObject {

        private String containerCode;

        private String warehouseCode;
    }
}
