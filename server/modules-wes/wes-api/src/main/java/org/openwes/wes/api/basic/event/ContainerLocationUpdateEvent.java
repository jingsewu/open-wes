package org.openwes.wes.api.basic.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.openwes.domain.event.api.DomainEvent;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ContainerLocationUpdateEvent extends DomainEvent {
    private String warehouseCode;
    private Long warehouseAreaId;
    private String containerCode;
    private String locationCode;
}
