package org.openwes.wes.api.outbound.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "出库计划单")
public class TransferContainerReleaseDTO implements Serializable {

    @Schema(title = "仓库编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String warehouseCode;

    @Schema(title = "周转箱号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String transferContainerCode;
}
