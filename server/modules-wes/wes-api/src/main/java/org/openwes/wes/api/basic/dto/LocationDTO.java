package org.openwes.wes.api.basic.dto;

import org.openwes.wes.api.basic.constants.LocationStatusEnum;
import org.openwes.wes.api.basic.constants.LocationTypeEnum;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "库位信息")
public class LocationDTO {

    @Hidden
    private Long id;

    @Schema(title = "库位编码")
    private String locationCode;

    @Schema(title = "巷道编码")
    private String aisleCode;

    @Schema(title = "货架编码")
    private String shelfCode;

    @Schema(title = "仓库编码")
    private String warehouseCode;

    @Hidden
    private Long warehouseAreaId;

    @Hidden
    private Long warehouseLogicId;

    @Schema(title = "库位类型")
    private LocationTypeEnum locationType;

    @Schema(title = "库位热度")
    private String heat;

    @Schema(title = "是否被占用")
    private boolean occupied;

    @Schema(title = "库位状态")
    private LocationStatusEnum locationStatus;

    @Schema(title = "位置信息")
    private PositionDTO position;

    @Hidden
    private long version;
}
