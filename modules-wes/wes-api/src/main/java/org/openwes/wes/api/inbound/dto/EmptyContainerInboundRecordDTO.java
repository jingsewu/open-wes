package org.openwes.wes.api.inbound.dto;

import org.openwes.common.utils.validate.ValidObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
@ValidObject
@Schema(description = "空箱入库记录")
public class EmptyContainerInboundRecordDTO {

    @NotEmpty
    @Schema(title = "容器规格")
    private String containerSpecCode;

    @Schema(title = "容器编号")
    @NotEmpty
    private String containerCode;

    @Schema(title = "位置编码")
    private String locationCode;
}
