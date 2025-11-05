package org.openwes.wes.api.basic.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.openwes.domain.event.api.DomainEvent;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class ContainerLocationUpdateEvent extends DomainEvent {
    private String warehouseCode;
    private Long warehouseAreaId;
    private String containerCode;
    private String locationCode;
}
