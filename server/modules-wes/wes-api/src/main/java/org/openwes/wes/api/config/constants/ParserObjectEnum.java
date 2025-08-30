package org.openwes.wes.api.config.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.wes.api.stock.dto.SkuBatchAttributeDTO;

@Getter
@AllArgsConstructor
public enum ParserObjectEnum implements IEnum {

    SKU_CODE("SKU_CODE", "skuCode"),
    BAR_CODE("BAR_CODE", "barCode"),
    CONTAINER_CODE("CONTAINER_CODE", "containerCode"),
    CONTAINER_FACE("CONTAINER_FACE", "containerFace"),
    AMOUNT("AMOUNT", "amount"),
    INBOUND_DATE(SkuBatchAttributeDTO.INBOUND_DATE, "inboundDate"),
    PRODUCT_DATE(SkuBatchAttributeDTO.PRODUCT_DATE, "productDate"),
    EXPIRED_DATE(SkuBatchAttributeDTO.EXPIRED_DATE, "expiredDate");

    private final String value;
    private final String label;
}
