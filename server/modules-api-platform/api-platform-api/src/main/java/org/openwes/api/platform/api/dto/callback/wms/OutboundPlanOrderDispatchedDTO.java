package org.openwes.api.platform.api.dto.callback.wms;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Data
@Schema(description = "出库封箱完成后回调数据")
public class OutboundPlanOrderDispatchedDTO implements Serializable {

    private String warehouseCode;

    private List<String> customerOrderNos;

    private List<String> customerOrderType;

}
