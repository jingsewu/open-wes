package org.openwes.wes.api.inbound.dto;

import org.openwes.wes.api.inbound.constants.PutAwayTaskStatusEnum;
import lombok.Data;

import java.util.List;

@Data
public class EmptyContainerInboundOrderDTO {

    private Long id;

    private String orderNo;

    private int planCount;

    private PutAwayTaskStatusEnum inboundStatus;

    List<EmptyContainerInboundOrderDetailDTO> details;
}
