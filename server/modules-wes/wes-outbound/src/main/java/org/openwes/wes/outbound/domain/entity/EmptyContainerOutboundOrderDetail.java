package org.openwes.wes.outbound.domain.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.wes.api.outbound.constants.EmptyContainerOutboundDetailStatusEnum;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class EmptyContainerOutboundOrderDetail {

    private Long id;

    private Long emptyContainerOutboundOrderId;

    private Long containerId;

    private String containerCode;

    @Builder.Default
    private EmptyContainerOutboundDetailStatusEnum detailStatus = EmptyContainerOutboundDetailStatusEnum.UNDO;

    public void initialize(Long emptyContainerOutboundOrderId) {
        this.emptyContainerOutboundOrderId = emptyContainerOutboundOrderId;
    }

    public void complete() {
        log.info("empty container outbound order: {} detail: {} completed", this.emptyContainerOutboundOrderId, this.id);
        this.detailStatus = EmptyContainerOutboundDetailStatusEnum.DONE;
    }

    public boolean isCompleted() {
        return this.detailStatus == EmptyContainerOutboundDetailStatusEnum.DONE ||
                this.detailStatus == EmptyContainerOutboundDetailStatusEnum.CANCELED;
    }
}
