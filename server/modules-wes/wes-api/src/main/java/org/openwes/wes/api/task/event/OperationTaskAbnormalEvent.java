package org.openwes.wes.api.task.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.openwes.domain.event.api.DomainEvent;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class OperationTaskAbnormalEvent extends DomainEvent {

    private OperationTaskAbnormalDetail detail;

    public OperationTaskAbnormalEvent(Long operationTaskId, OperationTaskAbnormalDetail detail) {
        super(operationTaskId);
        this.detail = detail;
    }

    @Data
    @Accessors(chain = true)
    public static class OperationTaskAbnormalDetail {
        private Long pickingOrderId;
        private Long pickingOrderDetailId;
        private Integer abnormalQty;
    }
}
