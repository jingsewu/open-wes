package org.openwes.wes.api.config.dto;

import org.openwes.common.utils.exception.WmsException;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;
import java.util.Map;

import static org.openwes.common.utils.exception.code_enum.BasicErrorDescEnum.BATCH_ATTRIBUTE_IS_REQUIRED;

@Data
public class BatchAttributeConfigDTO {

    private Long id;

    @NotEmpty
    private String code;
    @NotEmpty
    private String name;

    private String ownerCode;
    private String skuFirstCategory;

    private boolean enable;

    private Long version;

    @NotEmpty
    private List<BatchAttributeFieldConfigDTO> batchAttributeFieldConfigs;

    public void validateBatchAttribute(Map<String, Object> batchAttributes) {

        for (BatchAttributeFieldConfigDTO fieldConfig : batchAttributeFieldConfigs) {
            String fieldCode = fieldConfig.getFieldCode();
            if (fieldConfig.isRequired() && (batchAttributes == null || ObjectUtils.isEmpty(batchAttributes.get(fieldCode)))) {
                throw WmsException.throwWmsException(BATCH_ATTRIBUTE_IS_REQUIRED, fieldCode);
            }
        }

    }

    @Data
    public static class BatchAttributeFieldConfigDTO {

        @NotEmpty
        private String fieldCode;
        @NotEmpty
        private String fieldName;
        private boolean enable;
        private boolean required;
        private boolean exactMatch;
        private boolean keyAttribute;

        //this index mapping skuBatchAttribute 's flat attributes(1-10)
        private int index;
    }
}
