package org.openwes.station.domain.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openwes.station.application.business.handler.event.inbound.CallContainerEvent;
import org.openwes.wes.api.ems.proxy.dto.ContainerTaskDTO;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class InboundWorkStationCache extends WorkStationCache {

    private List<String> callContainers;
    private List<ContainerTaskCache> containerTasks;

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

        if (this.arrivedContainers == null) {
            log.warn("work station: {} code: {} haven't any containers.", this.id, this.stationCode);
            return;
        }

        if (this.callContainers != null) {
            this.callContainers.remove(containerCode);
        }
        if (this.containerTasks != null) {
            this.containerTasks.removeIf(v -> StringUtils.equals(v.getContainerCode(), containerCode));
        }
    }


    @Getter
    @Setter
    @Builder
    public static class ContainerTaskCache {
        private String containerCode;
        private String taskCode;
    }
}
