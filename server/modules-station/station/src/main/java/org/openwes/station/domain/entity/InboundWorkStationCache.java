package org.openwes.station.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.openwes.station.application.business.handler.event.inbound.CallContainerEvent;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class InboundWorkStationCache extends WorkStationCache {

    private List<String> callContainers;

    private List<String> taskCodes;

    public void saveCallContainers(CallContainerEvent callContainerEvent, List<String> taskCodes) {
        if (this.callContainers == null) {
            this.callContainers = callContainerEvent.getContainerCodes();
        } else {
            this.callContainers.addAll(callContainerEvent.getContainerCodes());
        }

        if (this.taskCodes == null) {
            this.taskCodes = taskCodes;
        } else {
            this.taskCodes.addAll(taskCodes);
        }
    }

    public void completeTasks() {
        this.arrivedContainers.forEach(container -> {
            this.taskCodes.removeAll(container.getTaskCodes());
            this.callContainers.remove(container.getContainerCode());
        });
    }
}
