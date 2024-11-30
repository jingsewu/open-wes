package org.openwes.station.infrastructure.persistence.po;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class OutboundWorkStationCachePO extends WorkStationCachePO {

    private String inputPutWallSlot;

    private String activePutWallCode;
}
