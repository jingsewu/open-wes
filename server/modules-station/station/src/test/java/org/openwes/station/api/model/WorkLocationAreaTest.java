package org.openwes.station.api.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openwes.station.api.constants.ProcessStatusEnum;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class WorkLocationAreaTest {

    private WorkLocationArea area;

    @BeforeEach
    void setUp() {
        area = new WorkLocationArea();
        List<WorkLocationArea.WorkLocationSlot> slots = new ArrayList<>();
        slots.add(createSlot("slot-1", "wl-1", "group-A"));
        slots.add(createSlot("slot-2", "wl-1", "group-A"));

        WorkLocationArea.WorkLocationView view = new WorkLocationArea.WorkLocationView();
        view.setWorkLocationCode("wl-1");
        view.setWorkLocationType("ROBOT");
        view.setEnable(true);
        view.setStationCode("ST-1");
        view.setWorkLocationSlots(slots);

        area.setWorkLocationViews(new ArrayList<>(List.of(view)));
    }

    private WorkLocationArea.WorkLocationSlot createSlot(String slotCode, String wlCode, String groupCode) {
        WorkLocationArea.WorkLocationSlot slot = new WorkLocationArea.WorkLocationSlot();
        slot.setSlotCode(slotCode);
        slot.setWorkLocationCode(wlCode);
        slot.setGroupCode(groupCode);
        slot.setLevel(1);
        slot.setBay(1);
        slot.setEnable(true);
        return slot;
    }

    private ArrivedContainerCache createContainer(String containerCode, String locationCode, String wlCode, String groupCode, ProcessStatusEnum status) {
        ArrivedContainerCache c = new ArrivedContainerCache();
        c.setContainerCode(containerCode);
        c.setLocationCode(locationCode);
        c.setWorkLocationCode(wlCode);
        c.setGroupCode(groupCode);
        c.setLevel(1);
        c.setBay(1);
        c.setProcessStatus(status);
        return c;
    }

    @Test
    void placeContainer_onExistingSlot_setsArrivedContainer() {
        ArrivedContainerCache container = createContainer("C-1", "slot-1", "wl-1", "group-A", ProcessStatusEnum.UNDO);
        area.placeContainer(container);

        WorkLocationArea.WorkLocationSlot slot = area.getWorkLocationViews().get(0).getWorkLocationSlots().get(0);
        assertNotNull(slot.getArrivedContainer());
        assertEquals("C-1", slot.getArrivedContainer().getContainerCode());
    }

    @Test
    void placeContainer_noneExisting_createsDynamicSlot() {
        ArrivedContainerCache container = createContainer("C-2", "slot-dynamic", "wl-1", "group-A", ProcessStatusEnum.UNDO);
        area.placeContainer(container);

        List<WorkLocationArea.WorkLocationSlot> slots = area.getWorkLocationViews().get(0).getWorkLocationSlots();
        assertEquals(3, slots.size());
        assertEquals("C-2", slots.get(2).getArrivedContainer().getContainerCode());
        assertEquals("slot-dynamic", slots.get(2).getSlotCode());
    }

    @Test
    void removeContainer_clearsSlot() {
        ArrivedContainerCache container = createContainer("C-1", "slot-1", "wl-1", "group-A", ProcessStatusEnum.UNDO);
        area.placeContainer(container);
        assertNotNull(area.getWorkLocationViews().get(0).getWorkLocationSlots().get(0).getArrivedContainer());

        area.removeContainer("C-1");
        assertNull(area.getWorkLocationViews().get(0).getWorkLocationSlots().get(0).getArrivedContainer());
    }

    @Test
    void removeProceedContainers_removesOnlyFullyProceedGroups() {
        ArrivedContainerCache c1 = createContainer("C-1", "slot-1", "wl-1", "group-A", ProcessStatusEnum.PROCEED);
        ArrivedContainerCache c2 = createContainer("C-2", "slot-2", "wl-1", "group-A", ProcessStatusEnum.PROCEED);
        area.placeContainer(c1);
        area.placeContainer(c2);

        List<ArrivedContainerCache> removed = area.removeProceedContainers();
        assertEquals(2, removed.size());
        assertFalse(area.hasContainers());
    }

    @Test
    void removeProceedContainers_keepsMixedStatusGroups() {
        ArrivedContainerCache c1 = createContainer("C-1", "slot-1", "wl-1", "group-A", ProcessStatusEnum.PROCEED);
        ArrivedContainerCache c2 = createContainer("C-2", "slot-2", "wl-1", "group-A", ProcessStatusEnum.UNDO);
        area.placeContainer(c1);
        area.placeContainer(c2);

        List<ArrivedContainerCache> removed = area.removeProceedContainers();
        assertTrue(removed.isEmpty());
        assertTrue(area.hasContainers());
    }

    @Test
    void getUndoContainers_returnsUndoAndProcessing() {
        ArrivedContainerCache c1 = createContainer("C-1", "slot-1", "wl-1", "group-A", ProcessStatusEnum.UNDO);
        ArrivedContainerCache c2 = createContainer("C-2", "slot-2", "wl-1", "group-A", ProcessStatusEnum.PROCESSING);
        area.placeContainer(c1);
        area.placeContainer(c2);

        List<ArrivedContainerCache> undo = area.getUndoContainers();
        assertEquals(2, undo.size());
    }

    @Test
    void setActiveContainerSlots_clearsOldAndSetsNew() {
        ArrivedContainerCache c1 = createContainer("C-1", "slot-1", "wl-1", "group-A", ProcessStatusEnum.PROCESSING);
        c1.setFace("A");
        area.placeContainer(c1);

        Set<String> slotCodes = Set.of("cell-1", "cell-2");
        area.setActiveContainerSlots("C-1", "A", slotCodes);

        WorkLocationArea.WorkLocationSlot slot = area.getWorkLocationViews().get(0).getWorkLocationSlots().get(0);
        assertEquals(slotCodes, slot.getActiveSlotCodes());

        // Setting new active slots should clear old ones
        Set<String> newSlotCodes = Set.of("cell-3");
        area.setActiveContainerSlots("C-1", "A", newSlotCodes);
        assertEquals(newSlotCodes, slot.getActiveSlotCodes());
    }

    @Test
    void hasContainers_returnsFalseWhenEmpty() {
        assertFalse(area.hasContainers());
    }
}
