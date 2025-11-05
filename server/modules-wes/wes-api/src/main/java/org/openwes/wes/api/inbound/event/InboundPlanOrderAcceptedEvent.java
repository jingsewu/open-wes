package org.openwes.wes.api.inbound.event;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.openwes.domain.event.api.DomainEvent;
import org.openwes.wes.api.inbound.dto.AcceptRecordDTO;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
public class InboundPlanOrderAcceptedEvent extends DomainEvent {

    private Long inboundPlanOrderId;
    private Long inboundPlanOrderDetailId;
    private String warehouseCode;

    private Long skuId;
    private Long workStationId;
    private Integer qtyAccepted;
    private Integer qtyAbnormal;
    private Map<String, Object> batchAttributes;

    @NotNull
    private AcceptTargetContainer targetContainer;

    public InboundPlanOrderAcceptedEvent(Long inboundPlanOrderId, Long inboundPlanOrderDetailId, String warehouseCode,
                                         Long skuId, Long workStationId, Integer qtyAccepted, Integer qtyAbnormal,
                                         Map<String, Object> batchAttributes,
                                         AcceptTargetContainer acceptTargetContainer) {
        super(inboundPlanOrderId);
        this.inboundPlanOrderId = inboundPlanOrderId;
        this.inboundPlanOrderDetailId = inboundPlanOrderDetailId;
        this.warehouseCode = warehouseCode;
        this.skuId = skuId;
        this.workStationId = workStationId;
        this.qtyAccepted = qtyAccepted;
        this.qtyAbnormal = qtyAbnormal;
        this.batchAttributes = batchAttributes;
        this.targetContainer = acceptTargetContainer;
    }

    @Accessors(chain = true)
    @Data
    public static class AcceptTargetContainer {
        private Long targetContainerId;
        private String targetContainerCode;
        private String targetContainerFace;
        private String targetContainerSlotCode;
        private String targetContainerSpecCode;

        public static AcceptTargetContainer build(AcceptRecordDTO acceptRecord) {
            return new InboundPlanOrderAcceptedEvent.AcceptTargetContainer()
                    .setTargetContainerCode(acceptRecord.getTargetContainerCode())
                    .setTargetContainerId(acceptRecord.getTargetContainerId())
                    .setTargetContainerFace(acceptRecord.getTargetContainerFace())
                    .setTargetContainerSlotCode(acceptRecord.getTargetContainerSlotCode())
                    .setTargetContainerSpecCode(acceptRecord.getTargetContainerSpecCode());
        }
    }
}
