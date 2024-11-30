package org.openwes.wes.api.stock.dto;

import org.openwes.wes.api.config.dto.BatchAttributeConfigDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
public class SkuBatchAttributeDTO implements Serializable, Comparable<SkuBatchAttributeDTO> {

    private Long id;

    private Long skuId;

    private Map<String, Object> skuAttributes;

    private String batchNo;

    /**
     * for task query to mapping
     */
    private List<Long> skuBatchStockIds;


    private static final String INBOUND_DATE = "inboundDate";

    public boolean match(BatchAttributeConfigDTO batchAttributeConfigDTO, Map<String, Object> batchAttributes) {

        if (batchAttributes == null) {
            return true;
        }

        return batchAttributeConfigDTO.getBatchAttributeFieldConfigs().stream().allMatch(config -> {
            if (!config.isEnable()) {
                return true;
            }

            if (config.isExactMatch()) {
                Object inputValue = batchAttributes.getOrDefault(config.getFieldCode(), "");
                Object existValue = skuAttributes.getOrDefault(config.getFieldCode(), "");
                return Objects.equals(inputValue, existValue);
            }

            return true;
        });
    }

    @Override
    public int compareTo(SkuBatchAttributeDTO skuBatchAttribute) {

        if (this.skuAttributes == null || skuBatchAttribute == null || skuBatchAttribute.getSkuAttributes() == null) {
            return -1;
        }

        String inboundDateA = String.valueOf(skuAttributes.getOrDefault(INBOUND_DATE, ""));
        String inboundDateB = String.valueOf(skuBatchAttribute.getSkuAttributes().getOrDefault(INBOUND_DATE, ""));
        return inboundDateA.compareTo(inboundDateB);
    }

}
