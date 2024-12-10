package org.openwes.wes.printer.application.event;

import org.openwes.domain.event.DomainEvent;
import org.openwes.wes.printer.domain.constants.ModuleEnum;
import org.openwes.wes.printer.domain.constants.PrintNodeEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
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
    private List<Map<String, Object>> targetArgs;
}
