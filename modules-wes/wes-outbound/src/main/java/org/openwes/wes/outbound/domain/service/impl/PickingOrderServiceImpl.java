package org.openwes.wes.outbound.domain.service.impl;

import com.google.common.collect.Lists;
import org.openwes.common.utils.id.OrderNoGenerator;
import org.openwes.wes.api.algo.IPickingOrderAlgoApi;
import org.openwes.wes.api.algo.PickingOrderAlgoApiFactory;
import org.openwes.wes.api.algo.dto.PickingOrderDispatchedResult;
import org.openwes.wes.api.algo.dto.PickingOrderHandlerContext;
import org.openwes.wes.api.algo.dto.PickingOrderReallocateContext;
import org.openwes.wes.api.algo.dto.PickingOrderReallocatedResult;
import org.openwes.wes.api.basic.ILocationApi;
import org.openwes.wes.api.basic.IWarehouseAreaApi;
import org.openwes.wes.api.basic.IWorkStationApi;
import org.openwes.wes.api.basic.constants.*;
import org.openwes.wes.api.basic.dto.LocationDTO;
import org.openwes.wes.api.basic.dto.PutWallSlotDTO;
import org.openwes.wes.api.basic.dto.WarehouseAreaDTO;
import org.openwes.wes.api.basic.dto.WorkStationDTO;
import org.openwes.wes.api.config.IBatchAttributeConfigApi;
import org.openwes.wes.api.config.dto.BatchAttributeConfigDTO;
import org.openwes.wes.api.main.data.ISkuMainDataApi;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.outbound.dto.OutboundAllocateSkuBatchContext;
import org.openwes.wes.api.stock.ISkuBatchAttributeApi;
import org.openwes.wes.api.stock.IStockApi;
import org.openwes.wes.api.stock.dto.ContainerStockDTO;
import org.openwes.wes.api.stock.dto.SkuBatchAttributeDTO;
import org.openwes.wes.api.stock.dto.SkuBatchStockDTO;
import org.openwes.wes.api.task.ITaskApi;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import org.openwes.wes.outbound.domain.entity.OutboundPreAllocatedRecord;
import org.openwes.wes.outbound.domain.entity.OutboundWave;
import org.openwes.wes.outbound.domain.entity.PickingOrder;
import org.openwes.wes.outbound.domain.entity.PickingOrderDetail;
import org.openwes.wes.outbound.domain.repository.OutboundPreAllocatedRecordRepository;
import org.openwes.wes.outbound.domain.repository.PickingOrderRepository;
import org.openwes.wes.outbound.domain.service.PickingOrderService;
import org.openwes.wes.outbound.domain.transfer.PickingOrderTransfer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class PickingOrderServiceImpl implements PickingOrderService {

    private static final int LIMIT_QUERY_OPERATION_TASK_COUNT = 2000;

    private final ILocationApi locationApi;
    private final IStockApi stockApi;
    private final ITaskApi taskApi;
    private final IWorkStationApi workStationApi;
    private final PickingOrderRepository pickingOrderRepository;
    private final OutboundPreAllocatedRecordRepository outboundPreAllocatedRecordRepository;
    private final PickingOrderTransfer pickingOrderTransfer;
    private final PickingOrderAlgoApiFactory pickingOrderAlgoApiFactory;
    private final ISkuBatchAttributeApi skuBatchAttributeApi;
    private final ISkuMainDataApi skuMainDataApi;
    private final IBatchAttributeConfigApi batchAttributeConfigFacade;
    private final IWarehouseAreaApi warehouseAreaApi;

    @Override
    public List<PickingOrder> spiltWave(OutboundWave outboundWave) {

        Map<Long, List<OutboundPreAllocatedRecord>> warehouseAreaRecordMap = outboundPreAllocatedRecordRepository
                .findByOutboundPlanOrderIds(outboundWave.getOutboundPlanOrderIds())
                .stream().collect(Collectors.groupingBy(OutboundPreAllocatedRecord::getWarehouseAreaId));

        List<PickingOrder> pickingOrders = Lists.newArrayList();
        warehouseAreaRecordMap.forEach((key, value) -> {

            List<PickingOrderDetail> pickingOrderDetails = value.stream()
                    .map(preAllocatedRecord -> {
                                PickingOrderDetail pickingOrderDetail = new PickingOrderDetail()
                                        .setSkuId(preAllocatedRecord.getSkuId())
                                        .setOwnerCode(preAllocatedRecord.getOwnerCode())
                                        .setOutboundOrderPlanId(preAllocatedRecord.getOutboundPlanOrderId())
                                        .setBatchAttributes(preAllocatedRecord.getBatchAttributes())
                                        .setQtyRequired(preAllocatedRecord.getQtyPreAllocated())
                                        .setSkuBatchStockId(preAllocatedRecord.getSkuBatchStockId())
                                        .setOutboundOrderPlanDetailId(preAllocatedRecord.getOutboundPlanOrderDetailId())
                                        .setRetargetingWarehouseAreaIds(preAllocatedRecord.getWarehouseAreaIds());
                                pickingOrderDetail.setModified(true);
                                return pickingOrderDetail;
                            }
                    ).toList();

            PickingOrder pickingOrder = new PickingOrder()
                    .setPriority(outboundWave.getPriority())
                    .setShortOutbound(outboundWave.isShortOutbound())
                    .setWarehouseCode(outboundWave.getWarehouseCode())
                    .setWarehouseAreaId(key)
                    .setPickingOrderNo(OrderNoGenerator.generationPickingOrderNo())
                    .setWaveNo(outboundWave.getWaveNo())
                    .setDetails(pickingOrderDetails);
            pickingOrder.setAllowReceive(true);
            pickingOrders.add(pickingOrder);
        });
        return pickingOrders;
    }

    @Override
    public List<OperationTaskDTO> allocateStocks(PickingOrderHandlerContext pickingOrderHandlerContext) {

        IPickingOrderAlgoApi pickingOrderAlgoApi = pickingOrderAlgoApiFactory.getPickingOrderAlgoApi(pickingOrderHandlerContext.getWarehouseAreaWorkType());

        return pickingOrderAlgoApi.allocateStocks(pickingOrderHandlerContext);
    }

    @Override
    public PickingOrderReallocatedResult reallocateStocks(PickingOrderReallocateContext pickingOrderReallocateContext) {

        IPickingOrderAlgoApi pickingOrderAlgoApi = pickingOrderAlgoApiFactory.getPickingOrderAlgoApi(pickingOrderReallocateContext.getWarehouseAreaWorkType());
        return pickingOrderAlgoApi.reallocateStocks(pickingOrderReallocateContext);
    }

    @Override
    public PickingOrderDispatchedResult dispatchOrders(PickingOrderHandlerContext pickingOrderHandlerContext) {

        IPickingOrderAlgoApi pickingOrderAlgoApi = pickingOrderAlgoApiFactory.getPickingOrderAlgoApi(pickingOrderHandlerContext.getWarehouseAreaWorkType());

        PickingOrderDispatchedResult pickingOrderDispatchedResult = pickingOrderAlgoApi.dispatchOrders(pickingOrderHandlerContext);

        if (pickingOrderDispatchedResult == null) {
            return null;
        }

        pickingOrderDispatchedResult.validate(pickingOrderHandlerContext);

        return pickingOrderDispatchedResult;
    }

    @Override
    public PickingOrderHandlerContext prepareFullContext(String warehouseCode, List<PickingOrder> pickingOrders) {

        List<WorkStationDTO> workStationDTOS = workStationApi.getByWarehouseCode(warehouseCode).stream()
                .filter(v -> WorkStationModeEnum.PICKING == v.getWorkStationMode())
                .filter(v -> v.getWorkStationStatus() == WorkStationStatusEnum.ONLINE).toList();

        if (CollectionUtils.isEmpty(workStationDTOS)) {
            log.debug("cannot find any online stations");
            return null;
        }

        List<PutWallSlotDTO> putWallSlotDTOS = workStationDTOS.stream()
                .flatMap(v -> v.getPutWalls().stream())
                .flatMap(v -> v.getPutWallSlots().stream().filter(PutWallSlotDTO::isEnable)).toList();

        boolean anyMatch = putWallSlotDTOS.stream().anyMatch(v -> v.getPutWallSlotStatus() == PutWallSlotStatusEnum.IDLE);
        if (!anyMatch) {
            log.debug("all station put wall slots are on busy");
            return null;
        }

        List<Long> assignedPickingOrderIds = putWallSlotDTOS.stream()
                .filter(v -> v.getPutWallSlotStatus() != PutWallSlotStatusEnum.IDLE)
                .map(PutWallSlotDTO::getPickingOrderId).toList();
        List<PickingOrder> undoAssignedPickingOrders = pickingOrderRepository.findOrderAndDetailsByPickingOrderIds(assignedPickingOrderIds);

        List<OperationTaskDTO> undoOperationTasks = CollectionUtils.isEmpty(assignedPickingOrderIds) ? Lists.newArrayList() : taskApi.queryOrderTasks(assignedPickingOrderIds, LIMIT_QUERY_OPERATION_TASK_COUNT);

        Set<Long> skuBatchStockIds = Stream.concat(pickingOrders.stream(), undoAssignedPickingOrders.stream())
                .flatMap(v -> v.getDetails().stream()).map(PickingOrderDetail::getSkuBatchStockId).collect(Collectors.toSet());

        List<ContainerStockDTO> allContainerStockDTOS = stockApi.getContainerStockBySkuBatchStockIds(skuBatchStockIds);
        Set<String> allContainerCodes = allContainerStockDTOS.stream().map(ContainerStockDTO::getContainerCode).collect(Collectors.toSet());
        List<LocationDTO> locations = locationApi.getByShelfCodes(allContainerCodes);

        Set<Pair<String, String>> containerAndFaceSet = undoOperationTasks.stream().map(v -> Pair.of(v.getSourceContainerCode(), v.getSourceContainerFace())).collect(Collectors.toSet());
        List<ContainerStockDTO> undoOperationTaskContainerStocks = allContainerStockDTOS.stream()
                .filter(v -> containerAndFaceSet.contains(Pair.of(v.getContainerCode(), v.getContainerFace()))).toList();

        return new PickingOrderHandlerContext()
                .setWarehouseCode(warehouseCode)
                .setWarehouseAreaWorkType(WarehouseAreaWorkTypeEnum.ROBOT)
                .setWorkStations(workStationDTOS)
                .setUndoAssignedPickingOrders(pickingOrderTransfer.toDTOs(undoAssignedPickingOrders))
                .setUndoOperationTasks(undoOperationTasks)
                .setUndoOperationTaskContainerStocks(undoOperationTaskContainerStocks)
                .setPickingOrders(pickingOrderTransfer.toDTOs(pickingOrders))
                .setContainerStocks(allContainerStockDTOS)
                .setLocations(locations);
    }

    @Override
    public PickingOrderHandlerContext prepareStockContext(String warehouseCode, List<PickingOrder> pickingOrders) {

        List<Long> skuBatchIds = pickingOrders.stream().flatMap(v -> v.getDetails()
                .stream().map(PickingOrderDetail::getSkuBatchStockId)).toList();
        List<ContainerStockDTO> containerStockDTOS = stockApi.getContainerStockBySkuBatchStockIds(skuBatchIds);

        return new PickingOrderHandlerContext()
                .setWarehouseCode(warehouseCode)
                .setPickingOrders(pickingOrderTransfer.toDTOs(pickingOrders))
                .setContainerStocks(containerStockDTOS)
                .setWarehouseAreaWorkType(WarehouseAreaWorkTypeEnum.MANUAL);
    }

    @Override
    public PickingOrderReallocateContext prepareReallocateStockContext(String warehouseCode, PickingOrder pickingOrder) {

        PickingOrderReallocateContext pickingOrderReallocateContext = new PickingOrderReallocateContext()
                .setWarehouseCode(warehouseCode)
                .setPickingOrder(pickingOrderTransfer.toDTO(pickingOrder));

        List<PickingOrderDetail> abnormalDetails = pickingOrder.getDetails().stream().filter(v -> v.getQtyAbnormal() > 0).toList();
        List<Long> skuBatchStockIds = abnormalDetails.stream().map(PickingOrderDetail::getSkuBatchStockId).toList();

        List<SkuBatchStockDTO> skuBatchStocks = stockApi.getSkuBatchStocks(skuBatchStockIds);
        boolean allMatch = skuBatchStocks.stream().allMatch(v -> {
            Integer totalAbnormalQty = abnormalDetails.stream().filter(a -> Objects.equals(a.getSkuBatchStockId(), v.getId())).map(PickingOrderDetail::getQtyAbnormal)
                    .reduce(Integer::sum).orElse(0);
            return v.getAvailableQty() >= totalAbnormalQty;
        });

        List<ContainerStockDTO> containerStockDTOS = stockApi.getContainerStockBySkuBatchStockIds(skuBatchStockIds);
        List<PickingOrderReallocateContext.PickingOrderReallocateDetail> pickingOrderReallocateDetails = abnormalDetails
                .stream().map(detail -> new PickingOrderReallocateContext.PickingOrderReallocateDetail()
                        .setPickingOrderDetailId(detail.getId())
                        .setSkuBatchStockId(detail.getSkuBatchStockId())
                        .setSkuBatchStocks(skuBatchStocks.stream().filter(v -> Objects.equals(v.getId(), detail.getSkuBatchStockId())).toList())
                        .setContainerStocks(containerStockDTOS.stream().filter(v -> Objects.equals(v.getSkuBatchStockId(), detail.getSkuBatchStockId())).toList())
                ).toList();

        pickingOrderReallocateContext.setPickingOrderReallocateDetails(pickingOrderReallocateDetails);

        if (allMatch) {
            return pickingOrderReallocateContext;
        }
        // match other batch attributes
        List<Long> skuIds = pickingOrder.getDetails().stream().map(PickingOrderDetail::getSkuId).toList();
        List<String> ownerCodes = pickingOrder.getDetails().stream().map(PickingOrderDetail::getOwnerCode).distinct().toList();
        OutboundAllocateSkuBatchContext outboundAllocateSkuBatchContext = this.prepareAllocateCache(skuIds, warehouseCode, ownerCodes);

        abnormalDetails.forEach(detail -> {
            List<SkuBatchStockDTO> skuBatchStockDTOS = outboundAllocateSkuBatchContext.matchSkuBatchStocks(detail.getSkuId(), detail.getOwnerCode(), detail.getBatchAttributes()).stream().filter(v -> !Objects.equals(v.getId(), detail.getSkuBatchStockId())).toList();

            if (CollectionUtils.isNotEmpty(detail.getRetargetingWarehouseAreaIds())) {
                skuBatchStockDTOS = skuBatchStockDTOS.stream().filter(k -> detail.getRetargetingWarehouseAreaIds().contains(k.getWarehouseAreaId())).toList();
            }

            List<ContainerStockDTO> containerStocks = stockApi.getContainerStockBySkuBatchStockIds(skuBatchStockDTOS.stream().map(SkuBatchStockDTO::getId).toList());

            pickingOrderReallocateContext.addPickingOrderReallocateDetail(detail.getId(), containerStocks, skuBatchStockDTOS);
        });

        return pickingOrderReallocateContext;
    }

    @Override
    public OutboundAllocateSkuBatchContext prepareAllocateCache(List<Long> skuIds, String warehouseCode, List<String> ownerCodes) {
        Map<Long, List<SkuBatchAttributeDTO>> skuBatchAttributeMap = skuBatchAttributeApi.getBySkuIds(skuIds)
                .stream().collect(Collectors.groupingBy(SkuBatchAttributeDTO::getSkuId));

        List<SkuMainDataDTO> skuMainDataDTOS = skuMainDataApi.getByIds(skuIds);
        Map<Long, String> skuCategoryMap = skuMainDataDTOS.stream().collect(Collectors.toMap(SkuMainDataDTO::getId,
                v -> Optional.ofNullable(v.getSkuAttribute().getSkuFirstCategory()).orElse(StringUtils.EMPTY)));

        Map<Pair<String, String>, BatchAttributeConfigDTO> skuBatchAttributeConfigMap = batchAttributeConfigFacade.getByOwners(ownerCodes).stream()
                .collect(Collectors.toMap(v -> Pair.of(v.getOwnerCode(), v.getSkuFirstCategory()), Function.identity(), (a, b) -> a));

        // enabled storage warehouse area
        List<WarehouseAreaDTO> storageWarehouseAreaDTOS = warehouseAreaApi.getByWarehouseCode(warehouseCode).stream()
                .filter(WarehouseAreaDTO::isEnable)
                .filter(dto -> WarehouseAreaTypeEnum.STORAGE_AREA == dto.getWarehouseAreaType()).toList();
        Map<Long, WarehouseAreaDTO> warehouseAreaMap = storageWarehouseAreaDTOS.stream()
                .collect(Collectors.toMap(WarehouseAreaDTO::getId, Function.identity()));

        List<Long> storageWarehouseAreaIds = storageWarehouseAreaDTOS.stream().map(WarehouseAreaDTO::getId).toList();

        List<Long> skuBatchAttributeIds = skuBatchAttributeMap.values()
                .stream().flatMap(Collection::stream).map(SkuBatchAttributeDTO::getId)
                .toList();
        List<SkuBatchStockDTO> skuBatchStockDTOS = stockApi.getBySkuBatchAttributeIds(skuBatchAttributeIds).stream()
                .filter(v -> v.getAvailableQty() > 0 && storageWarehouseAreaIds.contains(v.getWarehouseAreaId()))
                .toList();

        OutboundAllocateSkuBatchContext preAllocateCache = new OutboundAllocateSkuBatchContext();
        preAllocateCache.setSkuCategoryMap(skuCategoryMap);
        preAllocateCache.setSkuBatchAttributeMap(skuBatchAttributeMap);
        preAllocateCache.setSkuBatchAttributeConfigMap(skuBatchAttributeConfigMap);
        preAllocateCache.setWarehouseAreaMap(warehouseAreaMap);
        preAllocateCache.setSkuBatchStocks(skuBatchStockDTOS);
        return preAllocateCache;
    }

}
