package org.openwes.station.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openwes.station.api.constants.ProcessStatusEnum;

import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkLocationArea {
    private List<WorkLocationView> workLocationViews;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkLocationView {
        private String workLocationCode;
        private String workLocationType;
        private boolean enable;
        private String stationCode;
        private List<WorkLocationSlot> workLocationSlots;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkLocationSlot {
        private String slotCode;
        private String workLocationCode;
        private String groupCode;
        private int level;
        private int bay;
        private boolean enable;
        private ArrivedContainerCache arrivedContainer; // null = empty slot
        private Set<String> activeSlotCodes; // active container slots for current task
    }

    /**
     * Place a container onto its matching slot. If the slot doesn't exist
     * but the work location matches, create a dynamic slot (robot/cache shelf case).
     */
    public void placeContainer(ArrivedContainerCache container) {
        if (workLocationViews == null) return;
        for (WorkLocationView view : workLocationViews) {
            if (!view.isEnable() || view.getWorkLocationSlots() == null) continue;
            if (placeOnExistingSlot(view, container)) return;
        }
        // No existing slot found — try dynamic slot creation
        for (WorkLocationView view : workLocationViews) {
            if (!view.isEnable()) continue;
            if (view.getWorkLocationCode().equals(container.getWorkLocationCode())) {
                createDynamicSlot(view, container);
                return;
            }
        }
    }

    private boolean placeOnExistingSlot(WorkLocationView view, ArrivedContainerCache container) {
        for (WorkLocationSlot slot : view.getWorkLocationSlots()) {
            if (slot.getSlotCode().equals(container.getLocationCode())
                    && slot.getWorkLocationCode().equals(container.getWorkLocationCode())) {
                slot.setArrivedContainer(container);
                return true;
            }
        }
        return false;
    }

    private void createDynamicSlot(WorkLocationView view, ArrivedContainerCache container) {
        WorkLocationSlot newSlot = new WorkLocationSlot();
        newSlot.setSlotCode(container.getLocationCode());
        newSlot.setWorkLocationCode(view.getWorkLocationCode());
        newSlot.setGroupCode(container.getGroupCode());
        newSlot.setLevel(container.getLevel());
        newSlot.setBay(container.getBay());
        newSlot.setEnable(true);
        newSlot.setArrivedContainer(container);
        if (view.getWorkLocationSlots() == null) {
            view.setWorkLocationSlots(new ArrayList<>());
        }
        view.getWorkLocationSlots().add(newSlot);
    }

    public void removeContainer(String containerCode) {
        if (workLocationViews == null) return;
        for (WorkLocationView view : workLocationViews) {
            if (view.getWorkLocationSlots() == null) continue;
            view.getWorkLocationSlots().forEach(slot -> {
                if (slot.getArrivedContainer() != null
                        && slot.getArrivedContainer().getContainerCode().equals(containerCode)) {
                    slot.setArrivedContainer(null);
                }
            });
        }
    }

    public boolean hasContainers() {
        if (workLocationViews == null) return false;
        return workLocationViews.stream()
                .filter(v -> v.getWorkLocationSlots() != null)
                .flatMap(v -> v.getWorkLocationSlots().stream())
                .anyMatch(s -> s.getArrivedContainer() != null);
    }

    public List<ArrivedContainerCache> getAllContainers() {
        if (workLocationViews == null) return Collections.emptyList();
        return workLocationViews.stream()
                .filter(v -> v.getWorkLocationSlots() != null)
                .flatMap(v -> v.getWorkLocationSlots().stream())
                .map(WorkLocationSlot::getArrivedContainer)
                .filter(Objects::nonNull)
                .toList();
    }

    public List<ArrivedContainerCache> getContainersByStatus(ProcessStatusEnum status) {
        return getAllContainers().stream()
                .filter(c -> c.getProcessStatus() == status)
                .toList();
    }

    public List<ArrivedContainerCache> getUndoContainers() {
        return getAllContainers().stream()
                .filter(c -> c.getProcessStatus() == ProcessStatusEnum.UNDO
                        || c.getProcessStatus() == ProcessStatusEnum.PROCESSING)
                .toList();
    }

    public List<ArrivedContainerCache> getProcessingContainers() {
        return getContainersByStatus(ProcessStatusEnum.PROCESSING);
    }

    public List<ArrivedContainerCache> removeProceedContainers() {
        List<ArrivedContainerCache> allContainers = getAllContainers();
        Set<String> proceedGroupCodes = new HashSet<>();
        allContainers.stream()
                .collect(Collectors.groupingBy(ArrivedContainerCache::getGroupCode))
                .forEach((groupCode, containers) -> {
                    if (containers.stream().allMatch(c -> c.getProcessStatus() == ProcessStatusEnum.PROCEED)) {
                        proceedGroupCodes.add(groupCode);
                    }
                });

        List<ArrivedContainerCache> removed = allContainers.stream()
                .filter(c -> proceedGroupCodes.contains(c.getGroupCode()))
                .toList();

        if (workLocationViews != null) {
            workLocationViews.stream()
                    .filter(v -> v.getWorkLocationSlots() != null)
                    .flatMap(v -> v.getWorkLocationSlots().stream())
                    .forEach(slot -> {
                        if (slot.getArrivedContainer() != null
                                && proceedGroupCodes.contains(slot.getArrivedContainer().getGroupCode())) {
                            slot.setArrivedContainer(null);
                        }
                    });
        }
        return removed;
    }

    public void setActiveContainerSlots(String containerCode, String face, Set<String> slotCodes) {
        if (workLocationViews == null) return;
        workLocationViews.stream()
                .filter(v -> v.getWorkLocationSlots() != null)
                .flatMap(v -> v.getWorkLocationSlots().stream())
                .forEach(slot -> slot.setActiveSlotCodes(null));
        workLocationViews.stream()
                .filter(v -> v.getWorkLocationSlots() != null)
                .flatMap(v -> v.getWorkLocationSlots().stream())
                .filter(slot -> slot.getArrivedContainer() != null
                        && slot.getArrivedContainer().getContainerCode().equals(containerCode)
                        && Objects.equals(slot.getArrivedContainer().getFace(), face))
                .forEach(slot -> slot.setActiveSlotCodes(slotCodes));
    }

    public void setUndoContainersProcessing(boolean isOneStepRelocation) {
        List<ArrivedContainerCache> undoContainers = getContainersByStatus(ProcessStatusEnum.UNDO);
        if (undoContainers.isEmpty()) return;
        int limit = isOneStepRelocation ? 2 : 1;
        undoContainers.stream().limit(limit).forEach(ArrivedContainerCache::processing);
    }
}
