package org.openwes.api.platform.api.dto.callback.wms;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "出库封箱完成后回调数据")
public class ContainerSealedDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1229439573138664549L;

    @Schema(title = "仓库编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String warehouseCode;

    @Schema(title = "周转箱编号")
    private String transferContainerCode;

    @Schema(title = "封箱明细")
    private List<ContainerSealedDetailDTO> containerSealedDetailDTOS;

}
