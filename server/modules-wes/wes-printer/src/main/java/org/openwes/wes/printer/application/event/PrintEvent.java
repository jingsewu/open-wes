package org.openwes.wes.printer.application.event;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.openwes.domain.event.DomainEvent;
import org.openwes.wes.api.print.constants.ModuleEnum;
import org.openwes.wes.api.print.constants.PrintNodeEnum;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class PrintEvent extends DomainEvent {

    @NotEmpty
    private Long workStationId;
    @NotNull
    private ModuleEnum module;
    @NotNull
    private PrintNodeEnum printNode;

    private Object parameter;

    // targetArgs that impact on the template
    private Map<String, Object> targetArgs;
}
