package org.openwes.wes.stock.application;

import org.openwes.wes.api.stock.IStockAbnormalRecordApi;
import org.openwes.wes.api.stock.constants.StockAbnormalStatusEnum;
import org.openwes.wes.api.stock.dto.StockAbnormalRecordDTO;
import org.openwes.wes.stock.domain.aggregate.StockAbnormalAggregate;
import org.openwes.wes.stock.domain.entity.StockAbnormalRecord;
import org.openwes.wes.stock.domain.repository.StockAbnormalRecordRepository;
import org.openwes.wes.stock.domain.transfer.StockAbnormalRecordTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
@Validated
@RequiredArgsConstructor
public class StockAbnormalRecordApiImpl implements IStockAbnormalRecordApi {

    private final StockAbnormalRecordRepository stockAbnormalRecordRepository;
    private final StockAbnormalAggregate stockAbnormalAggregate;
    private final StockAbnormalRecordTransfer stockAbnormalRecordTransfer;

    @Override
    public List<StockAbnormalRecordDTO> createStockAbnormalRecords(List<StockAbnormalRecordDTO> stockAbnormalRecordDTOS) {
        List<StockAbnormalRecord> stockAbnormalRecords = stockAbnormalRecordTransfer.toDOs(stockAbnormalRecordDTOS);
        return stockAbnormalRecordTransfer.toDTOs(stockAbnormalAggregate.createStockAbnormalRecords(stockAbnormalRecords));
    }

    @Override
    public void createAdjustmentOrder(List<Long> ids) {
        List<StockAbnormalRecord> stockAbnormalRecords = stockAbnormalRecordRepository.findByIds(ids);
        stockAbnormalAggregate.createAdjustmentOrder(stockAbnormalRecords);
    }

    @Override
    public void manualClose(List<Long> ids) {
        List<StockAbnormalRecord> stockAbnormalRecords = stockAbnormalRecordRepository.findByIds(ids);
        stockAbnormalAggregate.manualClose(stockAbnormalRecords);
    }

    @Override
    public void upstreamClose(List<Long> ids) {
        List<StockAbnormalRecord> stockAbnormalRecords = stockAbnormalRecordRepository.findByIds(ids);
        stockAbnormalAggregate.upstreamClose(stockAbnormalRecords);
    }

    @Override
    public void completeAdjustment(List<Long> ids) {
        List<StockAbnormalRecord> stockAbnormalRecords = stockAbnormalRecordRepository.findByIds(ids);
        stockAbnormalAggregate.completeAdjustment(stockAbnormalRecords);
    }

    @Override
    public void createRecheckOrder(Collection<Long> ids) {
        List<StockAbnormalRecord> stockAbnormalRecords = stockAbnormalRecordRepository.findByIds(ids);
        stockAbnormalAggregate.createRecheckOrder(stockAbnormalRecords);
    }

    @Override
    public void createRecheckOrder(String replayNo, Collection<Long> ids) {
        List<StockAbnormalRecord> stockAbnormalRecords = stockAbnormalRecordRepository.findByIds(ids);
        stockAbnormalAggregate.createRecheckOrder(replayNo, stockAbnormalRecords);
    }

    @Override
    public List<StockAbnormalRecordDTO> getAllByIds(Collection<Long> ids) {
        List<StockAbnormalRecord> stockAbnormalRecords = stockAbnormalRecordRepository.findByIds(ids);
        return stockAbnormalRecordTransfer.toDTOs(stockAbnormalRecords);
    }

    @Override
    public List<StockAbnormalRecordDTO> getAllByContainerStockIdsAndStatues(Set<Long> containerStockIds, ArrayList<StockAbnormalStatusEnum> stockAbnormalStatusEnums) {

        stockAbnormalRecordRepository.findAllByContainerStockIdsAndStatues(containerStockIds, stockAbnormalStatusEnums);

        return List.of();
    }

    @Override
    public void recheckClose(List<StockAbnormalRecordDTO> invalidStockAbnormalRecordList) {
        //TODO implement it
    }

}
