package org.openwes.wes.stock.domain.entity;

import org.openwes.common.utils.base.UpdateUserDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
@NoArgsConstructor
public class SkuBatchAttribute extends UpdateUserDTO {

    private Long id;
    private Long skuId;
    private Map<String, Object> skuAttributes;
    private String batchNo;

    private String skuAttribute1;
    private String skuAttribute2;
    private String skuAttribute3;
    private String skuAttribute4;
    private String skuAttribute5;
    private String skuAttribute6;
    private String skuAttribute7;
    private String skuAttribute8;
    private String skuAttribute9;
    private String skuAttribute10;

    private Long version;

    public SkuBatchAttribute(Long skuId, Map<String, Object> skuAttributes) {
        this.skuId = skuId;
        this.skuAttributes = skuAttributes;
    }

    public boolean isSame(Map<String, Object> skuAttributes) {
        if (MapUtils.isEmpty(skuAttributes) && MapUtils.isEmpty(this.skuAttributes)) {
            return true;
        }

        if (MapUtils.isEmpty(skuAttributes) || MapUtils.isEmpty(this.skuAttributes)) {
            return false;
        }

        return skuAttributes.equals(this.skuAttributes);
    }

    public String getBatchNo() {
        if (StringUtils.isNotEmpty(this.batchNo)) {
            return this.batchNo;
        }

        if (MapUtils.isEmpty(this.skuAttributes)) {
            return "";
        }

        return DigestUtils.md5DigestAsHex(new HashMap<>(this.skuAttributes).toString().getBytes(StandardCharsets.UTF_8));
    }

}
