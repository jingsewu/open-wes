package org.openwes.wes.api.config.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class BarcodeParseResult implements Serializable {
    private String fieldName;
    private Object fieldValue;
}
