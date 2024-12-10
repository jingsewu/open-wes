package org.openwes.station.application.business.handler.event;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CustomApiEvent {

    @NotEmpty
    private String customApiCode;
    private Object event;
}
