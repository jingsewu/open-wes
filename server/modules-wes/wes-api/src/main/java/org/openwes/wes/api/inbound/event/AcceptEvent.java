package org.openwes.wes.api.inbound.event;

import org.openwes.domain.event.DomainEvent;
import lombok.*;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AcceptEvent extends DomainEvent {

    private Long inboundPlanOrderId;
    private Long inboundPlanOrderDetailId;
    private String warehouseCode;

    private Long skuId;
    private Long workStationId;
    private Integer qtyAccepted;
    private Map<String, Object> batchAttributes;

    private Long targetContainerId;
    private String targetContainerCode;
    private String targetContainerFace;
    private String targetContainerSlotCode;
    private String targetContainerSpecCode;
}
