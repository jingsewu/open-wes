package org.openwes.station.application.business.handler.event.inbound;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class CallContainerEvent implements Serializable{

    @NotEmpty
    private List<String> containerCodes;

    @NotEmpty
    private String warehouseCode;

}
