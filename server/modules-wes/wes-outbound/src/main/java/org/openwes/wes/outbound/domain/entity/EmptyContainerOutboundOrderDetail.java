package org.openwes.wes.outbound.domain.entity;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openwes.wes.api.outbound.constants.EmptyContainerOutboundDetailStatusEnum;

@Slf4j
@Data
public class EmptyContainerOutboundOrderDetail {

    private Long id;

    private Long emptyContainerOutboundOrderId;

    private Long containerId;

    private String containerCode;

    private EmptyContainerOutboundDetailStatusEnum detailStatus = EmptyContainerOutboundDetailStatusEnum.UNDO;

    public void complete() {
        log.info("empty container outbound order: {} detail: {} completed", this.emptyContainerOutboundOrderId, this.id);
        this.detailStatus = EmptyContainerOutboundDetailStatusEnum.DONE;
    }

    public boolean isCompleted() {
        return this.detailStatus == EmptyContainerOutboundDetailStatusEnum.DONE ||
                this.detailStatus == EmptyContainerOutboundDetailStatusEnum.CANCELED;
    }
}
