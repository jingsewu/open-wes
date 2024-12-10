package org.openwes.wes.stocktake.domain.aggregate;

import com.google.common.collect.Lists;
import org.openwes.wes.api.ems.proxy.dto.CreateContainerTaskDTO;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.stock.IStockAbnormalRecordApi;
import org.openwes.wes.api.stock.constants.StockAbnormalTypeEnum;
import org.openwes.wes.api.stock.dto.StockAbnormalRecordDTO;
import org.openwes.wes.api.stocktake.constants.StocktakeRecordStatusEnum;
import org.openwes.wes.api.stocktake.constants.StocktakeTaskDetailStatusEnum;
import org.openwes.wes.api.stocktake.dto.StocktakeRecordSubmitDTO;
import org.openwes.wes.common.facade.ContainerTaskApiFacade;
import org.openwes.wes.stocktake.domain.entity.StocktakeOrder;
import org.openwes.wes.stocktake.domain.entity.StocktakeRecord;
import org.openwes.wes.stocktake.domain.entity.StocktakeTask;
import org.openwes.wes.stocktake.domain.entity.StocktakeTaskDetail;
import org.openwes.wes.stocktake.domain.repository.StocktakeOrderRepository;
import org.openwes.wes.stocktake.domain.repository.StocktakeRecordRepository;
import org.openwes.wes.stocktake.domain.repository.StocktakeTaskRepository;
import org.openwes.wes.stocktake.domain.service.StocktakeTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StocktakeAggregate {

    private final StocktakeTaskService stocktakeTaskService;
    private final StocktakeOrderRepository stocktakeOrderRepository;
    private final StocktakeTaskRepository stocktakeTaskRepository;
    private final StocktakeRecordRepository stocktakeRecordRepository;
    private final IStockAbnormalRecordApi stockAbnormalRecordApi;
    private final ContainerTaskApiFacade containerTaskApiFacade;

    @Transactional(rollbackFor = Exception.class)
    public void executeStocktakeOrder(List<StocktakeTask> stocktakeTaskList, StocktakeOrder stocktakeOrder) {

        if (CollectionUtils.isEmpty(stocktakeTaskList)) {
            log.info("stocktake order: {} can not generate any stocktake tasks.", stocktakeOrder.getOrderNo());
            return;
        }

        stocktakeTaskRepository.saveAllTaskAndDetails(stocktakeTaskList);
        stocktakeOrder.execute();

        stocktakeOrderRepository.saveStocktakeOrder(stocktakeOrder);
    }

    @Transactional(rollbackFor = Exception.class)
    public void submitStocktakeRecord(StocktakeRecord stocktakeRecord, StocktakeTask stocktakeTask,
                                      SkuMainDataDTO skuMainDataDTO, StocktakeRecordSubmitDTO submitDTO) {
        stocktakeRecord.submit(submitDTO);
        stocktakeRecordRepository.save(stocktakeRecord);

        if (stocktakeRecord.getQtyAbnormal() != 0) {
            StockAbnormalRecordDTO stockAbnormalRecordDTO = new StockAbnormalRecordDTO()
                    .setOrderNo(stocktakeTask.getTaskNo())
                    .setWarehouseCode(stocktakeTask.getWarehouseCode())
                    .setOwnerCode(skuMainDataDTO.getOwnerCode())
                    .setContainerCode(stocktakeRecord.getContainerCode())
                    .setContainerSlotCode(stocktakeRecord.getContainerSlotCode())
                    .setContainerStockId(stocktakeRecord.getStockId())
                    .setSkuBatchStockId(stocktakeRecord.getSkuBatchStockId())
                    .setSkuBatchAttributeId(stocktakeRecord.getSkuBatchAttributeId())
                    .setSkuId(stocktakeRecord.getSkuId())
                    .setSkuCode(skuMainDataDTO.getSkuCode())
                    .setStockAbnormalType(StockAbnormalTypeEnum.STOCK_TAKE)
                    .setAbnormalReason(stocktakeRecord.getAbnormalReason().name())
                    .setReasonDesc(Optional.ofNullable(stocktakeRecord.getAbnormalReason()).map(String::valueOf).orElse(StringUtils.EMPTY))
                    .setQtyAbnormal(stocktakeRecord.getQtyAbnormal());
            stockAbnormalRecordApi.createStockAbnormalRecords(Lists.newArrayList(stockAbnormalRecordDTO));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public List<StocktakeRecord> createStocktakeRecord(List<StocktakeRecord> newRecordList, StocktakeTaskDetail stocktakeTaskDetail) {
        List<StocktakeRecord> recordList = stocktakeRecordRepository.saveAll(newRecordList);
        stocktakeTaskDetail.generateRecords();
        stocktakeTaskRepository.saveDetail(stocktakeTaskDetail);
        return recordList;
    }

    @Transactional(rollbackFor = Exception.class)
    public void receiveStocktakeOrder(List<StocktakeTask> stocktakeTasks, Long workStationId) {

        stocktakeTasks.forEach(stocktakeTask -> stocktakeTask.receive(workStationId));

        List<CreateContainerTaskDTO> createContainerTaskDTOS = stocktakeTasks.stream()
                .flatMap(stocktakeTask -> stocktakeTaskService.buildContainerTasks(
                                stocktakeTask.getDetails().stream().filter(v -> !v.isCompleted()).toList(), stocktakeTask)
                        .stream())
                .toList();

        stocktakeTaskRepository.saveAllTaskAndDetails(stocktakeTasks);

        containerTaskApiFacade.createContainerTasks(createContainerTaskDTOS);
    }

    @Transactional(rollbackFor = Exception.class)
    public void closeStocktakeTask(List<StocktakeTask> stocktakeTasks, List<StocktakeRecord> stocktakeRecords) {

        List<StocktakeTaskDetail> closeableDetails = stocktakeTasks.stream()
                .flatMap(v -> v.getDetails().stream().filter(d -> StocktakeTaskDetailStatusEnum.isCloseable(d.getStocktakeTaskDetailStatus())))
                .toList();

        stocktakeTasks.forEach(StocktakeTask::close);
        stocktakeTaskRepository.saveAllTaskAndDetails(stocktakeTasks);

        List<StocktakeRecord> newStocktakeRecords = stocktakeRecords.stream().filter(v -> v.getStocktakeRecordStatus() == StocktakeRecordStatusEnum.NEW).toList();
        newStocktakeRecords.forEach(StocktakeRecord::close);
        stocktakeRecordRepository.saveAll(newStocktakeRecords);

        containerTaskApiFacade.cancelTasks(closeableDetails.stream().map(StocktakeTaskDetail::getId).toList());
    }
}
