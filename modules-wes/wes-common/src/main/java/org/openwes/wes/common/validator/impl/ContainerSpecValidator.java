package org.openwes.wes.common.validator.impl;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.BasicErrorDescEnum;
import org.openwes.wes.api.basic.IContainerSpecApi;
import org.openwes.wes.api.basic.dto.ContainerSpecDTO;
import org.openwes.wes.common.validator.IValidator;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContainerSpecValidator implements IValidator<List<ContainerSpecValidator.ContainerSpecValidatorObject>, Void> {

    private final IContainerSpecApi containerSpecApi;

    @Override
    public Void validate(List<ContainerSpecValidatorObject> validateObjects) {
        Map<String, List<ContainerSpecValidatorObject>> validateObjectMap
                = validateObjects.stream().collect(Collectors.groupingBy(ContainerSpecValidatorObject::getWarehouseCode));

        for (Map.Entry<String, List<ContainerSpecValidatorObject>> entry : validateObjectMap.entrySet()) {
            String warehouseCode = entry.getKey();
            List<ContainerSpecValidatorObject> validatorObjects = entry.getValue();
            Set<String> containerSpecCodes = validatorObjects.stream()
                    .map(ContainerSpecValidatorObject::getContainerSpecCode)
                    .collect(Collectors.toSet());

            validate(containerSpecCodes, warehouseCode);
        }

        return null;
    }

    private void validate(Set<String> containerSpecCodes, String warehouseCode) {
        List<ContainerSpecDTO> containerSpecDTOS = containerSpecApi.queryContainerSpec(containerSpecCodes, warehouseCode);
        Set<String> existsContainerSpecCodes = containerSpecDTOS.stream()
                .map(ContainerSpecDTO::getContainerSpecCode).collect(Collectors.toSet());
        for (String containerSpecCode : containerSpecCodes) {
            if (!existsContainerSpecCodes.contains(containerSpecCode)) {
                throw WmsException.throwWmsException(BasicErrorDescEnum.CONTAINER_SPECIFIC_NOT_EXIST);
            }
        }
    }

    @Override
    public ValidatorName getValidatorName() {
        return ValidatorName.CONTAINER_SPEC;
    }

    @Data
    @Accessors(chain = true)
    @Builder
    public static class ContainerSpecValidatorObject {

        private String containerSpecCode;

        private String warehouseCode;
    }
}
