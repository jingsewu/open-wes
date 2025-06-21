package org.openwes.plugin.api.dto.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.openwes.domain.event.api.DomainEvent;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class LifeCycleStatusChangeEvent extends DomainEvent {
    private String newStatus;
    private Long entityId;
    private String entityNo;
    private String entityName;

    public LifeCycleStatusChangeEvent(String newStatus, Long entityId, String entityNo, String entityName) {
        this.newStatus = newStatus;
        this.entityId = entityId;
        this.entityNo = entityNo;
        this.entityName = entityName;
    }

    public LifeCycleStatusChangeEvent(String newStatus, Long entityId, String entityName) {
        this.newStatus = newStatus;
        this.entityId = entityId;
        this.entityName = entityName;
    }

    public LifeCycleStatusChangeEvent(String newStatus, String entityNo, String entityName) {
        this.newStatus = newStatus;
        this.entityNo = entityNo;
        this.entityName = entityName;
    }
}
