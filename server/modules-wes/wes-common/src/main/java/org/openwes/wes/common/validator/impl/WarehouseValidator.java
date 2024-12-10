package org.openwes.wes.common.validator.impl;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.BasicErrorDescEnum;
import org.openwes.wes.api.main.data.IWarehouseMainDataApi;
import org.openwes.wes.api.main.data.dto.WarehouseMainDataDTO;
import org.openwes.wes.common.validator.IValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseValidator implements IValidator<List<String>, Void> {

    private final IWarehouseMainDataApi warehouseMainDataApi;

    @Override
    public Void validate(List<String> warehouseCodes) {
        final Set<String> dataWarehouseCodes = warehouseMainDataApi.getWarehouses(warehouseCodes)
                .stream()
                .map(WarehouseMainDataDTO::getWarehouseCode)
                .collect(Collectors.toSet());

        if (warehouseCodes.stream().distinct().count() != dataWarehouseCodes.size()) {
            throw WmsException.throwWmsException(BasicErrorDescEnum.WAREHOUSE_CODE_NOT_EXIST, warehouseCodes);
        }
        return null;
    }

    @Override
    public ValidatorName getValidatorName() {
        return ValidatorName.WAREHOUSE;
    }
}
