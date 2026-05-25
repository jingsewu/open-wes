package org.openwes.station.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.openwes.wes.api.basic.constants.PutWallSlotStatusEnum;
import org.openwes.wes.api.basic.dto.PutWallDTO;
import org.openwes.wes.api.basic.dto.PutWallSlotDTO;
import org.openwes.wes.api.basic.dto.PutWallTagConfigDTO;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PutWallArea {
    private String activePutWallCode;
    private String inputPutWallSlot;
    private String putWallDisplayStyle;
    private PutWallTagConfigDTO putWallTagConfigDTO;
    private List<PutWallDTO> putWallViews;

    public void input(String slotCode) {
        this.inputPutWallSlot = slotCode;
    }

    public void clearInput() {
        this.inputPutWallSlot = null;
    }

    public void resetActivePutWall(Set<String> processingSlotCodes) {
        if (putWallViews == null) return;
        boolean match = putWallViews.stream()
                .flatMap(pw -> pw.getPutWallSlots().stream())
                .anyMatch(slot -> StringUtils.equals(this.activePutWallCode, slot.getPutWallCode())
                        && processingSlotCodes.contains(slot.getPutWallSlotCode()));
        if (!match) {
            this.activePutWallCode = putWallViews.stream()
                    .flatMap(pw -> pw.getPutWallSlots().stream())
                    .filter(slot -> processingSlotCodes.contains(slot.getPutWallSlotCode()))
                    .map(PutWallSlotDTO::getPutWallCode)
                    .findAny().orElse(null);
        }
    }

    public boolean hasWaitingBindingSlots() {
        if (putWallViews == null) return false;
        return putWallViews.stream()
                .flatMap(pw -> pw.getPutWallSlots().stream())
                .filter(PutWallSlotDTO::isEnable)
                .anyMatch(slot -> PutWallSlotStatusEnum.WAITING_BINDING == slot.getPutWallSlotStatus());
    }

    public Optional<PutWallSlotDTO> getSlot(String putWallSlotCode) {
        if (putWallViews == null) return Optional.empty();
        return putWallViews.stream()
                .flatMap(pw -> pw.getPutWallSlots().stream())
                .filter(slot -> StringUtils.equals(slot.getPutWallSlotCode(), putWallSlotCode))
                .findFirst();
    }

    public void validatePicking(PutWallSlotDTO putWallSlot) {
        if (putWallSlot.getPutWallSlotStatus() == PutWallSlotStatusEnum.IDLE) {
            throw new IllegalStateException("put wall slot is idle, please waiting order dispatched");
        }
        if (putWallSlot.getPutWallSlotStatus() == PutWallSlotStatusEnum.WAITING_BINDING) {
            throw new IllegalStateException("put wall slot wait binding, please bound first");
        }
        if (putWallSlot.getPutWallSlotStatus() == PutWallSlotStatusEnum.WAITING_SEAL) {
            throw new IllegalStateException("put wall slot wait sealing, please seal first");
        }
    }
}
