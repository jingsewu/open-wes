package org.openwes.station.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboundWorkStationCacheDTO extends WorkStationCacheDTO {

    private String inputPutWallSlot;

    private String activePutWallCode;
}
