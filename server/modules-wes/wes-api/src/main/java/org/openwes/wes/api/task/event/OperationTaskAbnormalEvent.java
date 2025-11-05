package org.openwes.wes.api.task.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.openwes.domain.event.api.DomainEvent;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class OperationTaskAbnormalEvent extends DomainEvent {

    private Long pickingOrderId;
    private Long pickingOrderDetailId;
    private Integer abnormalQty;

    public OperationTaskAbnormalEvent(Long operationTaskId, Long pickingOrderId,Long pickingOrderDetailId,Integer abnormalQty) {
        super(operationTaskId);
        this.pickingOrderId = pickingOrderId;
        this.pickingOrderDetailId = pickingOrderDetailId;
        this.abnormalQty = abnormalQty;
    }

}
