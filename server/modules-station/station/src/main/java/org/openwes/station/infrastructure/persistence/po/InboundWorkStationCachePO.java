package org.openwes.station.infrastructure.persistence.po;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openwes.station.domain.entity.InboundWorkStationCache;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
public class InboundWorkStationCachePO extends WorkStationCachePO {
    private List<String> callContainers;
    private List<InboundWorkStationCache.ContainerTaskCache> containerTasks;
}
