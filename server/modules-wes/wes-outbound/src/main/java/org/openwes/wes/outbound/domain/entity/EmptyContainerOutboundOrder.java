package org.openwes.wes.outbound.domain.entity;

import lombok.Data;
import org.openwes.common.utils.id.OrderNoGenerator;
import org.openwes.wes.api.outbound.constants.EmptyContainerOutboundStatusEnum;

import java.util.List;

@Data
public class EmptyContainerOutboundOrder {

    private Long id;

    private String warehouseCode;

    private Long warehouseAreaId;

    private String orderNo;

    private EmptyContainerOutboundStatusEnum emptyContainerOutboundStatus;

    private String containerSpecCode;

    private Integer planCount;

    private Integer actualCount;

    private Long workStationId;

    private List<EmptyContainerOutboundOrderDetail> details;

    public void initial() {
        this.orderNo = OrderNoGenerator.generationEmptyContainerOutboundOrderNo();
        this.emptyContainerOutboundStatus = EmptyContainerOutboundStatusEnum.NEW;
    }

    public void execute() {
        if (this.emptyContainerOutboundStatus != EmptyContainerOutboundStatusEnum.NEW) {
            throw new IllegalStateException("The empty container outbound order status is not NEW, can not execute");
        }
        this.emptyContainerOutboundStatus = EmptyContainerOutboundStatusEnum.PENDING;
    }
}
