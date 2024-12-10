package org.openwes.station.api.vo;

import org.openwes.station.api.dto.ArrivedContainerCacheDTO;
import org.openwes.wes.api.basic.dto.WorkStationDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * extends WorkLocationDTO to add the arrived container on the location.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkLocationExtend extends WorkStationDTO.WorkLocation<WorkLocationExtend.WorkLocationSlotExtend> {

    private List<WorkLocationSlotExtend> workLocationSlots;

    @EqualsAndHashCode(callSuper = true)
    @Data
    @Accessors(chain = true)
    public static class WorkLocationSlotExtend extends WorkStationDTO.WorkLocationSlot {
        private ArrivedContainerCacheDTO arrivedContainer;
    }

}
