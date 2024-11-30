package org.openwes.wes.api.outbound.event;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class PickingOrderAbnormalEvent {

    private List<PickingOrderAbnormalDetail> details;

    @Data
    @Accessors(chain = true)
    public static class PickingOrderAbnormalDetail {
        private Long pickingOrderId;
        private Long pickingOrderDetailId;
        private Integer abnormalQty;
    }
}
