package org.openwes.wes.common.validator.impl;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.BasicErrorDescEnum;
import org.openwes.wes.api.main.data.IOwnerMainDataApi;
import org.openwes.wes.api.main.data.dto.OwnerMainDataDTO;
import org.openwes.wes.common.validator.IValidator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerValidator implements IValidator<OwnerValidator.OwnerValidatorObject, Void> {

    private final IOwnerMainDataApi ownerMainDataApi;

    @Override
    public Void validate(OwnerValidatorObject ownerValidatorObject) {
        if (CollectionUtils.isEmpty(ownerValidatorObject.getOwnerCodes())) {
            throw WmsException.throwWmsException(BasicErrorDescEnum.OWNER_CODE_NOT_EXIST, ownerValidatorObject.getOwnerCodes());
        }
        final Set<String> dataOwnerCodes = ownerMainDataApi.getOwners(ownerValidatorObject.getOwnerCodes(), ownerValidatorObject.getWarehouseCode())
                .stream()
                .map(OwnerMainDataDTO::getOwnerCode)
                .collect(Collectors.toSet());

        if (ownerValidatorObject.getOwnerCodes().stream().distinct().count() != dataOwnerCodes.size()) {
            throw WmsException.throwWmsException(BasicErrorDescEnum.OWNER_CODE_NOT_EXIST, ownerValidatorObject.getOwnerCodes());
        }
        return null;
    }

    @Override
    public ValidatorName getValidatorName() {
        return ValidatorName.OWNER;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OwnerValidatorObject {
        private Collection<String> ownerCodes;
        private String warehouseCode;
    }
}
