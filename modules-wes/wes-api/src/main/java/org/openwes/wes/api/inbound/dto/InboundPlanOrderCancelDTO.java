package org.openwes.wes.api.inbound.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class InboundPlanOrderCancelDTO implements Serializable {


    @Serial
    private static final long serialVersionUID = 4118336824167005439L;

    private List<String> identifyNos;
    private String warehouseCode;

}
