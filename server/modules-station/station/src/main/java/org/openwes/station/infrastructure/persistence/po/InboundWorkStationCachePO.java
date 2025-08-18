package org.openwes.station.infrastructure.persistence.po;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class InboundWorkStationCachePO extends WorkStationCachePO {
    private List<String> callContainers;
    private List<String> taskCodes;
}
