package org.openwes.station.infrastructure.persistence.po;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class InboundWorkStationCachePO extends WorkStationCachePO {
    private List<String> callContainers;
    private Map<String, List<String>> containerTaskCodes;
}
