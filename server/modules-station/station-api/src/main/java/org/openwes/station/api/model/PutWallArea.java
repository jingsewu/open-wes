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
    private PutWallTagConfigDTO putWallTagConfigDTO = new PutWallTagConfigDTO();
    private List<PutWallDTO> putWallViews;

    public void input(String slotCode) {
        this.inputPutWallSlot = slotCode;
    }

    public void clearInput() {
        this.inputPutWallSlot = null;
    }

    public void setActivePutWallCode(String activePutWallCode) {
        this.activePutWallCode = activePutWallCode;
        syncActiveWall();
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
        syncActiveWall();
    }

    private void syncActiveWall() {
        if (putWallViews == null) return;
        putWallViews.forEach(pw -> pw.setActive(StringUtils.equals(pw.getPutWallCode(), this.activePutWallCode)));
    }

    public boolean hasWaitingBindingSlots() {
        if (putWallViews == null) return false;
        return putWallViews.stream()
                .flatMap(pw -> pw.getPutWallSlots().stream())
                .filter(PutWallSlotDTO::isEnable)
                .anyMatch(slot -> PutWallSlotStatusEnum.WAITING_BINDING == slot.getPutWallSlotStatus());
    }

    /**
     * Mark BOUND slots that hold PROCESSING tasks as DISPATCH (待分拨). The DB has
     * no DISPATCH state — it is a station-side, in-cache transition applied after
     * the SKU is scanned so the operator can tap the slot to confirm the pick.
     */
    public void markDispatch(Set<String> slotCodes) {
        if (putWallViews == null || slotCodes == null || slotCodes.isEmpty()) return;
        putWallViews.stream()
                .flatMap(pw -> pw.getPutWallSlots().stream())
                .filter(slot -> slotCodes.contains(slot.getPutWallSlotCode()))
                .filter(slot -> PutWallSlotStatusEnum.BOUND == slot.getPutWallSlotStatus())
                .forEach(slot -> slot.setPutWallSlotStatus(PutWallSlotStatusEnum.DISPATCH));
    }

    /**
     * Apply a snapshot of a slot's state to the put wall area.
     * This method has zero business logic — it copies fields from the incoming DTO
     * to the matching slot in the cache. The DTO is treated as the source of truth.
     */
    public void applySnapshot(PutWallSlotDTO slotDTO) {
        getSlot(slotDTO.getPutWallSlotCode()).ifPresent(slot -> {
            slot.setPutWallSlotStatus(slotDTO.getPutWallSlotStatus());
            slot.setPickingOrderId(slotDTO.getPickingOrderId());
            slot.setTransferContainerCode(slotDTO.getTransferContainerCode());
            slot.setTransferContainerRecordId(slotDTO.getTransferContainerRecordId());
            slot.setQtyDispatched(slotDTO.getQtyDispatched());
        });
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
