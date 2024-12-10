package org.openwes.wes.api.task.dto;

import lombok.Data;

@Data
public class OutboundSealContainerCallbackResponse {

    private Long outboundPlanOrderId;
    private String destination;
    private String transferContainerCode;
}
