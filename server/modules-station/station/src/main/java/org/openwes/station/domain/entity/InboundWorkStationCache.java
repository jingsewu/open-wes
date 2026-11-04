package org.openwes.station.domain.entity;

import com.google.common.collect.Lists;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openwes.station.application.business.handler.event.inbound.CallContainerEvent;
import org.openwes.wes.api.ems.proxy.dto.ContainerTaskDTO;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class InboundWorkStationCache extends WorkStationCache {

    public void saveCallContainers(CallContainerEvent callContainerEvent, List<ContainerTaskDTO> containerTaskDTOS) {
        if (this.callContainers == null) {
            this.callContainers = Lists.newArrayList(callContainerEvent.getContainerCodes());
        } else {
            this.callContainers.addAll(callContainerEvent.getContainerCodes());
        }

        List<ContainerTaskCache> containerTaskCaches = containerTaskDTOS.stream()
                .map(containerTaskDTO ->
                        ContainerTaskCache.builder().containerCode(containerTaskDTO.getContainerCode())
                                .taskCode(containerTaskDTO.getTaskCode()).build()).toList();

        if (this.containerTasks == null) {
            this.containerTasks = Lists.newArrayList(containerTaskCaches);
        } else {
            this.containerTasks.addAll(containerTaskCaches);
        }
    }

    public void completeTasks(String containerCode) {
        log.info("work station: {} code: {} complete tasks with container: {}", this.id, this.stationCode, containerCode);

        if (this.callContainers != null) {
            this.callContainers.remove(containerCode);
        }
        if (this.containerTasks != null) {
            this.containerTasks.removeIf(v -> StringUtils.equals(v.getContainerCode(), containerCode));
        }
    }

    // ContainerTaskCache is now in WorkStationCache (parent class)
}
