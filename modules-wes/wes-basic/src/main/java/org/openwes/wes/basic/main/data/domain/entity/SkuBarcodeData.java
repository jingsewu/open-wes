package org.openwes.wes.basic.main.data.domain.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SkuBarcodeData {

    private Long id;

    private Long skuId;

    private String skuCode;

    private String barCode;
}
