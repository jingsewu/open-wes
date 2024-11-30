package org.openwes.wes.common.validator.impl;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.BasicErrorDescEnum;
import org.openwes.wes.api.main.data.ISkuMainDataApi;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.common.validator.IValidator;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkuValidator implements IValidator<SkuValidator.SkuValidatorObject, Set<SkuMainDataDTO>> {

    private final ISkuMainDataApi skuMainDataApi;

    @Override
    public Set<SkuMainDataDTO> validate(SkuValidatorObject skuValidatorObject) {
        String ownerCode = skuValidatorObject.getOwnerCode();
        Set<SkuMainDataDTO> skuMainDataDTOS = skuMainDataApi.getSkuMainData(skuValidatorObject.getSkuCodes(), ownerCode)
                .stream()
                .filter(v -> skuValidatorObject.getOwnerCode().equals(v.getOwnerCode()))
                .collect(Collectors.toSet());

        if (skuValidatorObject.getSkuCodes().stream().distinct().count() != skuMainDataDTOS.size()) {
            throw WmsException.throwWmsException(BasicErrorDescEnum.SOME_SKU_CODE_NOT_EXIST, skuValidatorObject.getSkuCodes());
        }

        return skuMainDataDTOS;
    }

    @Override
    public ValidatorName getValidatorName() {
        return ValidatorName.SKU;
    }

    @Data
    @Accessors(chain = true)
    public static class SkuValidatorObject {
        private Set<String> skuCodes;
        private String ownerCode;

        public SkuValidatorObject(Set<String> skuCodes, String ownerCode) {
            this.skuCodes = skuCodes;
            this.ownerCode = ownerCode;
        }
    }


}
