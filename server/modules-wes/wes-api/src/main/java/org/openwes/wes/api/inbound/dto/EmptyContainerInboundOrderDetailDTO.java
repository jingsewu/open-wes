package org.openwes.wes.api.inbound.dto;

import org.openwes.wes.api.inbound.constants.PutAwayTaskStatusEnum;
import lombok.Data;

@Data
public class EmptyContainerInboundOrderDetailDTO {

    private Long id;

    private Long emptyContainerInboundOrderId;

    private String taskNo;

    private String containerCode;

    private String containerSpecCode;

    private String locationCode;

    private PutAwayTaskStatusEnum inboundStatus;
}
