package org.openwes.wes.algo.outbound.application;

import org.openwes.wes.algo.outbound.domain.entity.OrderAssignStationModel;
import org.openwes.wes.algo.outbound.domain.service.RobotAreaOrderAssignService;
import org.openwes.wes.algo.outbound.domain.service.StockAllocationService;
import org.openwes.wes.api.algo.IPickingOrderAlgoApi;
import org.openwes.wes.api.algo.dto.*;
import org.openwes.wes.api.basic.constants.PutWallSlotStatusEnum;
import org.openwes.wes.api.basic.constants.WarehouseAreaWorkTypeEnum;
import org.openwes.wes.api.basic.dto.PutWallSlotDTO;
import org.openwes.wes.api.basic.dto.WorkStationDTO;
import org.openwes.wes.api.config.ISystemConfigApi;
import org.openwes.wes.api.config.dto.SystemConfigDTO;
import org.openwes.wes.api.outbound.dto.PickingOrderDTO;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class RobotAreaPickingOrderAlgoApiImpl implements IPickingOrderAlgoApi {

    private final RobotAreaOrderAssignService orderAssignService;
    private final StockAllocationService robotAreaStockAllocationService;
    private final ISystemConfigApi systemConfigApi;

    @Override
    public WarehouseAreaWorkTypeEnum getWarehouseAreaWorkType() {
        return WarehouseAreaWorkTypeEnum.ROBOT;
    }

    private List<PickingOrderAssignedResult> assignOrders(PickingOrderHandlerContext pickingOrderHandlerContext) {

        List<WorkStationDTO> workStations = pickingOrderHandlerContext.getOnlinePickingModeStations();

        if (ObjectUtils.isEmpty(workStations)) {
            return Collections.emptyList();
        }

        pickingOrderHandlerContext.getPickingOrders().sort((v1, v2) -> v2.getPriority() - v1.getPriority());

        OrderAssignStationModel orderAssignStationModel = new OrderAssignStationModel(pickingOrderHandlerContext.getUndoOperationTasks(), pickingOrderHandlerContext.getUndoOperationTaskContainerStocks());

        List<PickingOrderDTO> pickingOrders = pickingOrderHandlerContext.getPickingOrders();

        for (PickingOrderDTO pickingOrderDTO : pickingOrders) {

            List<PutWallSlotDTO> putWallSlots = workStations.stream()
                    .filter(v -> Objects.equals(v.getWarehouseAreaId(), pickingOrderDTO.getWarehouseAreaId()))
                    .flatMap(workStationDTO -> workStationDTO.getPutWalls().stream())
                    .flatMap(putWallDTO -> putWallDTO.getPutWallSlots().stream())
                    .toList();

            List<PutWallSlotDTO> availableSlots = putWallSlots.stream()
                    .filter(PutWallSlotDTO::isEnable)
                    .filter(v -> v.getPutWallSlotStatus() == PutWallSlotStatusEnum.IDLE).toList();
            if (ObjectUtils.isEmpty(availableSlots)) {
                break;
            }

            orderAssignService.matchingAvailablePutWallSlots(pickingOrderDTO, availableSlots);
            if (ObjectUtils.isEmpty(pickingOrderDTO.getAvailablePutWallSlots())) {
                continue;
            }

            List<PutWallSlotDTO> availablePutWallSlots = putWallSlots.stream()
                    .filter(v ->
                            pickingOrderDTO.getAvailablePutWallSlots().stream()
                                    .anyMatch(availablePutWallSlot ->
                                            availablePutWallSlot.getWorkStationId().equals(v.getWorkStationId())
                                                    && availablePutWallSlot.getPutWallSlotCode().equals(v.getPutWallSlotCode()))).toList();
            Map<Long, List<PutWallSlotDTO>> stationPutWallSlotMap = availablePutWallSlots.stream()
                    .collect(Collectors.groupingBy(PutWallSlotDTO::getWorkStationId));

            long score = 0;
            Long workStationId = 0L;
            for (Map.Entry<Long, List<PutWallSlotDTO>> entry : stationPutWallSlotMap.entrySet()) {
                long tmpScore = orderAssignService.calculateStationScore(entry.getKey(), orderAssignStationModel,
                        pickingOrderDTO.getDetails());
                if (tmpScore >= score) {
                    workStationId = entry.getKey();
                    score = tmpScore;
                }
            }
            orderAssignService.assign(pickingOrderDTO, workStationId, availablePutWallSlots);
        }

        return pickingOrderHandlerContext.getPickingOrders()
                .stream().filter(v -> ObjectUtils.isNotEmpty(v.getAssignedStationSlot()))
                .map(v -> new PickingOrderAssignedResult()
                        .setPickingOrderId(v.getId())
                        .setAssignedStationSlot(v.getAssignedStationSlot())).toList();
    }

    @Override
    public List<OperationTaskDTO> allocateStocks(PickingOrderHandlerContext pickingOrderHandlerContext) {

        return pickingOrderHandlerContext.getPickingOrders().stream()
                .flatMap(pickingOrder -> robotAreaStockAllocationService.allocateSinglePickingOrder(pickingOrder, pickingOrderHandlerContext))
                .toList();
    }

    @Override
    public PickingOrderReallocatedResult reallocateStocks(PickingOrderReallocateContext pickingOrderReallocateContext) {
        return robotAreaStockAllocationService.reallocateSinglePickingOrder(pickingOrderReallocateContext);
    }

    @Override
    public PickingOrderDispatchedResult dispatchOrders(PickingOrderHandlerContext pickingOrderHandlerContext) {

        SystemConfigDTO.OutboundAlgoConfigDTO outboundAlgoConfig = systemConfigApi.getOutboundAlgoConfig();
        if (outboundAlgoConfig.isUseLocalAlgorithm()) {

            List<PickingOrderAssignedResult> pickingOrderAssignedResults = assignOrders(pickingOrderHandlerContext);
            if (ObjectUtils.isEmpty(pickingOrderAssignedResults)) {
                log.info("picking orders can't be assigned maybe there are not put wall slot rules matched");
                return null;
            }

            pickingOrderHandlerContext.removeNotAssignedPickingOrders(pickingOrderAssignedResults);
            pickingOrderHandlerContext.assignPickingOrders(pickingOrderAssignedResults);

            List<OperationTaskDTO> operationTaskDTOS = allocateStocks(pickingOrderHandlerContext);

            return new PickingOrderDispatchedResult().setAssignedResults(pickingOrderAssignedResults).setOperationTaskDTOS(operationTaskDTOS);
        }

        return null;
    }
}
