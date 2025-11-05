package org.openwes.wes.api.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "周转箱释放DTO")
public class TransferContainerReleaseDTO implements Serializable {

    @Schema(title = "仓库编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String warehouseCode;

    @Schema(title = "周转箱号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String transferContainerCode;
}
