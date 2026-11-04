package org.openwes.station.api.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openwes.wes.api.basic.constants.PutWallSlotStatusEnum;
import org.openwes.wes.api.basic.dto.PutWallDTO;
import org.openwes.wes.api.basic.dto.PutWallSlotDTO;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PutWallAreaTest {

    private PutWallArea putWallArea;

    @BeforeEach
    void setUp() {
        putWallArea = new PutWallArea();

        PutWallSlotDTO slot1 = new PutWallSlotDTO();
        slot1.setPutWallCode("PW-1");
        slot1.setPutWallSlotCode("PS-1");
        slot1.setEnable(true);
        slot1.setPutWallSlotStatus(PutWallSlotStatusEnum.BOUND);

        PutWallSlotDTO slot2 = new PutWallSlotDTO();
        slot2.setPutWallCode("PW-2");
        slot2.setPutWallSlotCode("PS-2");
        slot2.setEnable(true);
        slot2.setPutWallSlotStatus(PutWallSlotStatusEnum.WAITING_BINDING);

        PutWallDTO pw1 = new PutWallDTO();
        pw1.setPutWallCode("PW-1");
        pw1.setPutWallSlots(List.of(slot1));

        PutWallDTO pw2 = new PutWallDTO();
        pw2.setPutWallCode("PW-2");
        pw2.setPutWallSlots(List.of(slot2));

        putWallArea.setPutWallViews(new ArrayList<>(List.of(pw1, pw2)));
        putWallArea.setActivePutWallCode("PW-1");
    }

    @Test
    void resetActivePutWall_switchesWhenCurrentDoesntMatch() {
        Set<String> processingSlotCodes = Set.of("PS-2");
        putWallArea.resetActivePutWall(processingSlotCodes);

        assertEquals("PW-2", putWallArea.getActivePutWallCode());
    }

    @Test
    void resetActivePutWall_keepsWhenCurrentMatches() {
        Set<String> processingSlotCodes = Set.of("PS-1");
        putWallArea.resetActivePutWall(processingSlotCodes);

        assertEquals("PW-1", putWallArea.getActivePutWallCode());
    }

    @Test
    void hasWaitingBindingSlots_checksEnabledOnly() {
        assertTrue(putWallArea.hasWaitingBindingSlots());

        // Disable the waiting_binding slot
        putWallArea.getPutWallViews().get(1).getPutWallSlots().get(0).setEnable(false);
        assertFalse(putWallArea.hasWaitingBindingSlots());
    }

    @Test
    void validatePicking_throwsOnIdle() {
        PutWallSlotDTO slot = new PutWallSlotDTO();
        slot.setPutWallSlotStatus(PutWallSlotStatusEnum.IDLE);

        assertThrows(IllegalStateException.class, () -> putWallArea.validatePicking(slot));
    }

    @Test
    void validatePicking_throwsOnWaitingBinding() {
        PutWallSlotDTO slot = new PutWallSlotDTO();
        slot.setPutWallSlotStatus(PutWallSlotStatusEnum.WAITING_BINDING);

        assertThrows(IllegalStateException.class, () -> putWallArea.validatePicking(slot));
    }

    @Test
    void validatePicking_throwsOnWaitingSeal() {
        PutWallSlotDTO slot = new PutWallSlotDTO();
        slot.setPutWallSlotStatus(PutWallSlotStatusEnum.WAITING_SEAL);

        assertThrows(IllegalStateException.class, () -> putWallArea.validatePicking(slot));
    }
}
