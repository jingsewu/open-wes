package org.openwes.wes.stocktake.application;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.StocktakeErrorDescEnum;
import org.openwes.wes.api.main.data.ISkuMainDataApi;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.stock.IStockApi;
import org.openwes.wes.api.stock.dto.ContainerStockDTO;
import org.openwes.wes.api.stocktake.IStocktakeApi;
import org.openwes.wes.api.stocktake.constants.StocktakeOrderStatusEnum;
import org.openwes.wes.api.stocktake.constants.StocktakeRecordStatusEnum;
import org.openwes.wes.api.stocktake.constants.StocktakeTaskDetailStatusEnum;
import org.openwes.wes.api.stocktake.constants.StocktakeTaskStatusEnum;
import org.openwes.wes.api.stocktake.dto.*;
import org.openwes.wes.stocktake.domain.aggregate.StocktakeAggregate;
import org.openwes.wes.stocktake.domain.entity.*;
import org.openwes.wes.stocktake.domain.repository.StocktakeOrderRepository;
import org.openwes.wes.stocktake.domain.repository.StocktakeRecordRepository;
import org.openwes.wes.stocktake.domain.repository.StocktakeTaskRepository;
import org.openwes.wes.stocktake.domain.service.StocktakeOrderService;
import org.openwes.wes.stocktake.domain.service.StocktakeRecordService;
import org.openwes.wes.stocktake.domain.transfer.StocktakeOrderTransfer;
import org.openwes.wes.stocktake.domain.transfer.StocktakeRecordTransfer;
import org.openwes.wes.stocktake.domain.transfer.StocktakeTaskTransfer;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@DubboService
public class StocktakeApiImpl implements IStocktakeApi {

    private final StocktakeOrderRepository stocktakeOrderRepository;
    private final StocktakeTaskRepository stocktakeTaskRepository;
    private final StocktakeRecordRepository stocktakeRecordRepository;
    private final StocktakeOrderService stocktakeOrderService;
    private final StocktakeRecordService stocktakeRecordService;
    private final StocktakeAggregate stocktakeAggregate;
    private final StocktakeOrderTransfer stocktakeOrderTransfer;
    private final StocktakeTaskTransfer stocktakeTaskTransfer;
    private final StocktakeRecordTransfer stocktakeRecordTransfer;
    private final IStockApi stockApi;
    private final ISkuMainDataApi skuMainDataApi;

    @Override
    public void createStocktakeOrder(StocktakeOrderCreateDTO stocktakeOrderCreateDTO) {
        stocktakeOrderCreateDTO.transfer();

        StocktakeOrder stocktakeOrder = stocktakeOrderTransfer.fromCreateDTO(stocktakeOrderCreateDTO);
        List<StocktakeOrderDetail> details = stocktakeOrderTransfer.toDetails(stocktakeOrderCreateDTO);
        stocktakeOrder.setDetails(details);

        stocktakeOrder.initialize();

        stocktakeOrderRepository.saveStocktakeOrder(stocktakeOrder);
    }

    @Override
    public List<String> cancelStocktakeOrder(StocktakeOrderCancelDTO stocktakeOrderCancelDTO) {
        List<StocktakeOrder> cancelableOrders = stocktakeOrderRepository.findAllByOrderNosAndWarehouseCodeAndStatuses(
                stocktakeOrderCancelDTO.getOrderNos(),
                stocktakeOrderCancelDTO.getWarehouseCode(),
                StocktakeOrderStatusEnum.CANCELABLE_LIST
        );

        List<StocktakeOrder> canceledOrders = stocktakeOrderService.cancelStocktakeOrder(cancelableOrders);
        canceledOrders = stocktakeOrderRepository.saveAllOrderAndDetails(canceledOrders);

        return canceledOrders.stream().map(StocktakeOrder::getOrderNo).toList();
    }

    @Override
    public void executeStocktakeOrder(StocktakeOrderExecuteDTO stocktakeOrderExecuteDTO) {
        // 查询可执行的盘点单
        List<StocktakeOrder> executableOrders = stocktakeOrderRepository.findAllByOrderNosAndWarehouseCodeAndStatuses(
                stocktakeOrderExecuteDTO.getOrderNos(),
                stocktakeOrderExecuteDTO.getWarehouseCode(),
                StocktakeOrderStatusEnum.EXECUTABLE_LIST
        );

        if (CollectionUtils.isEmpty(executableOrders)) {
            throw WmsException.throwWmsException(StocktakeErrorDescEnum.STOCKTAKE_ORDER_NOT_FOUND_OR_EXECUTED);
        }

        executableOrders.forEach(stocktakeOrder -> {
            List<StocktakeTask> stocktakeTaskList = stocktakeOrderService.splitStocktakeOrder(stocktakeOrder, stocktakeOrderExecuteDTO.getTaskCount());
            stocktakeAggregate.executeStocktakeOrder(stocktakeTaskList, stocktakeOrder);
        });
    }

    @Override
    public void receiveStocktakeOrder(List<Long> stocktakeOrderIds, Long workStationId) {
        List<StocktakeTask> stocktakeTasks = stocktakeTaskRepository.findAllById(stocktakeOrderIds);
        stocktakeAggregate.receiveStocktakeOrder(stocktakeTasks, workStationId);
    }

    @Override
    public void submitStocktakeRecord(StocktakeRecordSubmitDTO submitDTO) {
        StocktakeRecord stocktakeRecord = stocktakeRecordRepository.findById(submitDTO.getRecordId());
        StocktakeTask stocktakeTask = stocktakeTaskRepository.findById(stocktakeRecord.getStocktakeTaskId());
        SkuMainDataDTO skuMainDataDTO = skuMainDataApi.getById(stocktakeRecord.getSkuId());

        stocktakeOrderService.validateSubmit(stocktakeRecord);
        stocktakeAggregate.submitStocktakeRecord(stocktakeRecord, stocktakeTask, skuMainDataDTO, submitDTO);
    }

    @Override
    public void closeStocktakeTask(StocktakeTaskCloseDTO closeDTO) {

        List<StocktakeTask> stocktakeTasks = stocktakeTaskRepository.findAllById(closeDTO.getTaskIdList())
                .stream().filter(v -> StocktakeTaskStatusEnum.CLOSEABLE_LIST.contains(v.getStocktakeTaskStatus()))
                .toList();

        if (CollectionUtils.isEmpty(stocktakeTasks)) {
            return;
        }

        List<Long> stocktakeTaskIds = stocktakeTasks.stream().map(StocktakeTask::getId).toList();
        List<StocktakeRecord> closeableRecordList = stocktakeRecordRepository
                .findAllByTaskIdAndStatuses(stocktakeTaskIds, StocktakeRecordStatusEnum.CLOSEABLE_LIST);

        stocktakeAggregate.closeStocktakeTask(stocktakeTasks, closeableRecordList);
    }

    @Override
    public void closeStocktakeTask(Long workStationId) {
        List<StocktakeTask> stocktakeTasks = stocktakeTaskRepository.findAllByWorkStationIdAndStatus(workStationId, StocktakeTaskStatusEnum.CLOSEABLE_LIST);

        if (CollectionUtils.isEmpty(stocktakeTasks)) {
            return;
        }

        List<Long> stocktakeTaskIds = stocktakeTasks.stream().map(StocktakeTask::getId).toList();
        List<StocktakeRecord> closeableRecordList = stocktakeRecordRepository
                .findAllByTaskIdAndStatuses(stocktakeTaskIds, StocktakeRecordStatusEnum.CLOSEABLE_LIST);

        stocktakeAggregate.closeStocktakeTask(stocktakeTasks, closeableRecordList);
    }

    @Override
    public List<StocktakeRecordDTO> generateStocktakeRecords(String containerCode, String face, Long workStationId) {

        List<StocktakeTask> stocktakeTasks = stocktakeTaskRepository.findAllByWorkStationIdAndStatus(workStationId,
                Lists.newArrayList(StocktakeTaskStatusEnum.NEW, StocktakeTaskStatusEnum.STARTED));
        if (ObjectUtils.isEmpty(stocktakeTasks)) {
            return Collections.emptyList();
        }

        List<Long> stocktakeTaskIds = stocktakeTasks.stream().map(StocktakeTask::getId).toList();
        List<StocktakeTaskDetail> stocktakeTaskDetails = stocktakeTaskRepository.findAllByContainerCodeAndFaceAndStocktakeTaskId(containerCode, face, stocktakeTaskIds);
        if (ObjectUtils.isEmpty(stocktakeTaskDetails)) {
            return Collections.emptyList();
        }

        StocktakeTaskDetail stocktakeTaskDetail = stocktakeTaskDetails.get(0);

        if (stocktakeTaskDetail.getStocktakeTaskDetailStatus() == StocktakeTaskDetailStatusEnum.STARTED) {
            List<StocktakeRecord> stocktakeRecords = stocktakeRecordRepository.findAllByTaskDetailId(stocktakeTaskDetail.getId())
                    .stream().filter(v -> v.getStocktakeRecordStatus() == StocktakeRecordStatusEnum.NEW).toList();

            return stocktakeRecordTransfer.toDTOS(stocktakeRecords);
        }
        StocktakeOrder stocktakeOrder = stocktakeOrderRepository.findById(stocktakeTaskDetail.getStocktakeOrderId());
        List<ContainerStockDTO> containerStockDTOs = stockApi.getByContainerAndFace(stocktakeOrder.getWarehouseCode(),
                stocktakeTaskDetail.getContainerCode(), stocktakeTaskDetail.getContainerFace());

        List<StocktakeRecord> stocktakeRecords = stocktakeRecordService.generateStocktakeRecords(stocktakeTaskDetail, stocktakeOrder, containerStockDTOs);

        List<StocktakeRecord> savedList = stocktakeAggregate.createStocktakeRecord(stocktakeRecords, stocktakeTaskDetail);

        return stocktakeRecordTransfer.toDTOS(savedList);
    }

    @Override
    public List<StocktakeTaskDTO> getStocktakeTasksByWorkStationId(Long workStationId) {
        List<StocktakeTask> stocktakeTasks = stocktakeTaskRepository.findAllTasksByWorkStationIdAndStatus(workStationId,
                Lists.newArrayList(StocktakeTaskStatusEnum.NEW, StocktakeTaskStatusEnum.STARTED));
        return stocktakeTaskTransfer.toDTOS(stocktakeTasks);
    }
}
