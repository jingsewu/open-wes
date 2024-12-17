package org.openwes.wes.outbound.domain.entity;


import lombok.Data;
import org.openwes.wes.api.outbound.constants.EmptyContainerOutboundDetailStatusEnum;

@Data
public class EmptyContainerOutboundOrderDetail {

    private Long id;

    private Long emptyContainerOutboundOrderId;

    private Long containerId;

    private String containerCode;

    private EmptyContainerOutboundDetailStatusEnum detailStatus = EmptyContainerOutboundDetailStatusEnum.UNDO;

}
