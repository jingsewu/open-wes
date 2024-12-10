package org.openwes.wes.inbound.domain.entity;

import org.openwes.wes.api.inbound.constants.PutAwayTaskStatusEnum;
import lombok.Data;

@Data
public class EmptyContainerInboundOrderDetail {

    private Long id;

    private Long emptyContainerInboundOrderId;

    private String containerCode;

    private String containerSpecCode;

    private String locationCode;

    private PutAwayTaskStatusEnum inboundStatus;

    public void initial() {
        inboundStatus = PutAwayTaskStatusEnum.NEW;
    }

    public void complete() {
        this.inboundStatus = PutAwayTaskStatusEnum.PUTTED_AWAY;
    }
}
