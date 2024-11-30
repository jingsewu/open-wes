package org.openwes.wes.api.basic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class ContainerLocationReportDTO implements Serializable {

    @NotEmpty
    @Schema(title = "仓库编码")
    private String warehouseCode;

    @NotNull
    @Schema(title = "库区ID")
    private Long warehouseAreaId;

    @NotEmpty
    @Schema(title = "容器编码")
    private String containerCode;

    @NotEmpty
    @Schema(title = "库位编码")
    private String locationCode;
}
