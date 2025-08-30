
package org.openwes.wes.api.config.dto;

import lombok.Builder;
import lombok.Getter;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Builder
@Getter
public class BarcodeParseResult implements Serializable {
    private String amount;
    private String containerCode;
    private String containerFace;
    private List<SkuMainDataDTO> skus;
    private Map<String, String> attributes;
}
