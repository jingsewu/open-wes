package org.openwes.station.domain.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.openwes.station.application.business.handler.event.inbound.CallContainerEvent;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class InboundWorkStationCache extends WorkStationCache {

    private List<String> callContainers;
    private Map<String, List<String>> containerTaskCodes;

    public void saveCallContainers(CallContainerEvent callContainerEvent, Map<String, List<String>> containerTaskCodes) {
        if (this.callContainers == null) {
            this.callContainers = Lists.newArrayList(callContainerEvent.getContainerCodes());
        } else {
            this.callContainers.addAll(callContainerEvent.getContainerCodes());
        }

        if (this.containerTaskCodes == null) {
            this.containerTaskCodes = Maps.newHashMap(containerTaskCodes);
        } else {
            this.containerTaskCodes.putAll(containerTaskCodes);
        }
    }

    public void completeTasks(String containerCode) {

        log.info("work station: {} code: {} complete tasks with container: {}", this.id, this.stationCode, containerCode);

        if (this.arrivedContainers == null) {
            log.warn("work station: {} code: {} haven't any containers.", this.id, this.stationCode);
            return;
        }

        if (this.callContainers != null) {
            this.callContainers.remove(containerCode);
        }
        if (this.containerTaskCodes != null) {
            this.containerTaskCodes.remove(containerCode);
        }
    }
}
