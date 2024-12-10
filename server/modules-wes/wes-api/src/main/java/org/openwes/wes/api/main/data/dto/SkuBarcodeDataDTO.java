package org.openwes.wes.api.main.data.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SkuBarcodeDataDTO implements Serializable {


    @Serial
    private static final long serialVersionUID = 8521067010240150416L;

    private Long id;

    private Long skuId;

    private String skuCode;

    private String barCode;

}
