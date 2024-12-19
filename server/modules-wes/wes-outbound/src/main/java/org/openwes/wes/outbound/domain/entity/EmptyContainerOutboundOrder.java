package org.openwes.wes.outbound.domain.entity;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openwes.common.utils.id.OrderNoGenerator;
import org.openwes.wes.api.outbound.constants.EmptyContainerOutboundOrderStatusEnum;

import java.util.List;

@Slf4j
@Data
public class EmptyContainerOutboundOrder {

    private Long id;

    private String warehouseCode;

    private Long warehouseAreaId;

    private String orderNo;

    private EmptyContainerOutboundOrderStatusEnum emptyContainerOutboundStatus;

    private String containerSpecCode;

    private Integer planCount;

    private Integer actualCount;

    private Long workStationId;

    private List<EmptyContainerOutboundOrderDetail> details;

    public void initial() {
        this.orderNo = OrderNoGenerator.generationEmptyContainerOutboundOrderNo();
        this.emptyContainerOutboundStatus = EmptyContainerOutboundOrderStatusEnum.NEW;
    }

    public void execute() {
        log.info("empty container outbound order: {} execute", this.id);

        if (this.emptyContainerOutboundStatus != EmptyContainerOutboundOrderStatusEnum.NEW) {
            throw new IllegalStateException("The empty container outbound order status is not NEW, can not execute");
        }
        this.emptyContainerOutboundStatus = EmptyContainerOutboundOrderStatusEnum.PENDING;
    }

    public void cancel() {
        log.info("empty container outbound order: {} cancel", this.id);

        if (this.emptyContainerOutboundStatus != EmptyContainerOutboundOrderStatusEnum.NEW
                && this.emptyContainerOutboundStatus != EmptyContainerOutboundOrderStatusEnum.PENDING) {
            throw new IllegalStateException("The empty container outbound order status is not NEW or PENDING, can not cancel");
        }

        this.emptyContainerOutboundStatus = EmptyContainerOutboundOrderStatusEnum.CANCELED;
    }

    public void complete(List<Long> emptyContainerOutboundOrderDetailIds) {

        this.details.stream().filter(detail -> emptyContainerOutboundOrderDetailIds.contains(detail.getId())).forEach(EmptyContainerOutboundOrderDetail::complete);

        if (this.details.stream().allMatch(EmptyContainerOutboundOrderDetail::isCompleted)) {
            log.info("empty container outbound order: {} complete", this.id);
            this.emptyContainerOutboundStatus = EmptyContainerOutboundOrderStatusEnum.FINISHED;
        }
    }
}
