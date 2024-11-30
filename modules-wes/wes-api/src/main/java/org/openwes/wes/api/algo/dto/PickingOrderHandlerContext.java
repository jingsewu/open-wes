package org.openwes.wes.api.algo.dto;

import com.google.common.collect.Maps;
import org.openwes.wes.api.basic.constants.WarehouseAreaWorkTypeEnum;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.openwes.wes.api.basic.constants.WorkStationStatusEnum;
import org.openwes.wes.api.basic.dto.LocationDTO;
import org.openwes.wes.api.basic.dto.WorkStationDTO;
import org.openwes.wes.api.outbound.dto.PickingOrderDTO;
import org.openwes.wes.api.stock.dto.ContainerStockDTO;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Accessors(chain = true)
public class PickingOrderHandlerContext {

    @NotEmpty
    private String warehouseCode;

    private List<PickingOrderDTO> undoAssignedPickingOrders;

    @NotEmpty
    private List<PickingOrderDTO> pickingOrders;

    private List<WorkStationDTO> workStations;

    private List<ContainerStockDTO> containerStocks;

    private List<OperationTaskDTO> undoOperationTasks;

    private List<ContainerStockDTO> undoOperationTaskContainerStocks;

    private List<LocationDTO> locations;

    @NotNull
    private WarehouseAreaWorkTypeEnum warehouseAreaWorkType;

    private Map<Long, Set<String>> stationAllocatedContainers = Maps.newHashMap();

    public void assignPickingOrders(List<PickingOrderAssignedResult> pickingOrderAssignedResults) {
        this.pickingOrders.forEach(pickingOrderDTO -> pickingOrderAssignedResults.forEach(assignedResult -> {
            if (assignedResult.getPickingOrderId().equals(pickingOrderDTO.getId())) {
                pickingOrderDTO.setAssignedStationSlot(assignedResult.getAssignedStationSlot());
            }
        }));
    }

    public void removeNotAssignedPickingOrders(List<PickingOrderAssignedResult> pickingOrderAssignedResults) {
        Set<Long> assignedPickingOrderNos = pickingOrderAssignedResults.stream()
                .map(PickingOrderAssignedResult::getPickingOrderId).collect(Collectors.toSet());
        this.pickingOrders.removeIf(v -> !assignedPickingOrderNos.contains(v.getId()));
    }

    public List<WorkStationDTO> getOnlinePickingModeStations() {
        return this.workStations.stream()
                .filter(v -> v.getWorkStationMode() == WorkStationModeEnum.PICKING)
                .filter(v -> v.getWorkStationStatus() == WorkStationStatusEnum.ONLINE)
                .filter(v -> ObjectUtils.isNotEmpty(v.getPutWalls())).toList();
    }
}
