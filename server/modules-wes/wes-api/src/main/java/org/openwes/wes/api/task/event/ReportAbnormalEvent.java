package org.openwes.wes.api.task.event;

import org.openwes.domain.event.DomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ReportAbnormalEvent extends DomainEvent {

    private List<Long> taskIds;
}
