package org.openwes.wes.api.stock;

import org.openwes.wes.api.stock.constants.StockAbnormalStatusEnum;
import org.openwes.wes.api.stock.dto.StockAbnormalRecordDTO;
import jakarta.validation.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface IStockAbnormalRecordApi {

    List<StockAbnormalRecordDTO> createStockAbnormalRecords(List<StockAbnormalRecordDTO> stockAbnormalRecordDTOS);

    void createAdjustmentOrder(@NotEmpty List<Long> ids);

    void manualClose(List<Long> ids);

    void upstreamClose(@NotEmpty List<Long> ids);

    void completeAdjustment(List<Long> ids);

    void createRecheckOrder(Collection<Long> ids);

    void createRecheckOrder(String replayNo, Collection<Long> ids);

    List<StockAbnormalRecordDTO> getAllByIds(Collection<Long> ids);

    List<StockAbnormalRecordDTO> getAllByContainerStockIdsAndStatues(Set<Long> longs, ArrayList<StockAbnormalStatusEnum> stockAbnormalStatusEnums);

    void recheckClose(List<StockAbnormalRecordDTO> invalidStockAbnormalRecordList);
}
