package org.openwes.station.infrastructure.remote;

import org.openwes.wes.api.main.data.ISkuMainDataApi;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.stock.ISkuBatchAttributeApi;
import org.openwes.wes.api.stock.dto.SkuBatchAttributeDTO;
import org.openwes.wes.api.stocktake.IStocktakeApi;
import org.openwes.wes.api.stocktake.dto.StocktakeRecordDTO;
import org.openwes.wes.api.stocktake.dto.StocktakeRecordSubmitDTO;
import org.openwes.wes.api.task.constants.OperationTaskStatusEnum;
import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import org.openwes.wes.api.task.dto.OperationTaskVO;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Service
@RequiredArgsConstructor

public class StocktakeService {

    @DubboReference
    private IStocktakeApi stocktakeApi;
    @DubboReference
    private ISkuMainDataApi skuMainDataApi;
    @DubboReference
    private ISkuBatchAttributeApi skuBatchAttributeApi;

    public List<OperationTaskVO> generateStocktakeRecords(String containerCode, String face, Long workStationId) {
        List<StocktakeRecordDTO> stocktakeRecordDTOS = stocktakeApi.generateStocktakeRecords(containerCode, face, workStationId);

        List<OperationTaskDTO> operationTaskDTOS = stocktakeRecordDTOS.stream().map(v -> {
            OperationTaskDTO operationTaskDTO = new OperationTaskDTO();
            operationTaskDTO.setId(v.getId());
            operationTaskDTO.setTaskStatus(OperationTaskStatusEnum.NEW);
            operationTaskDTO.setTaskType(OperationTaskTypeEnum.STOCK_TAKE);
            operationTaskDTO.setContainerStockId(v.getStockId());
            operationTaskDTO.setDetailId(v.getId());
            operationTaskDTO.setRequiredQty(v.getQtyOriginal());
            operationTaskDTO.setAbnormalQty(0);
            operationTaskDTO.setOperatedQty(0);
            operationTaskDTO.setSourceContainerCode(containerCode);
            operationTaskDTO.setSourceContainerCode(face);
            operationTaskDTO.setSkuId(v.getSkuId());
            operationTaskDTO.setSkuBatchAttributeId(v.getSkuBatchAttributeId());
            return operationTaskDTO;
        }).toList();

        Set<Long> skuMainDataIds = operationTaskDTOS.stream()
                .map(OperationTaskDTO::getSkuId).collect(Collectors.toSet());
        Map<Long, SkuMainDataDTO> skuMainDataDTOMap = skuMainDataApi.getByIds(skuMainDataIds)
                .stream().collect(Collectors.toMap(SkuMainDataDTO::getId, v -> v));

        Set<Long> skuBatchAttributedIds = operationTaskDTOS.stream().map(OperationTaskDTO::getSkuBatchAttributeId).collect(Collectors.toSet());
        Map<Long, SkuBatchAttributeDTO> skuBatchAttributeDTOMap = skuBatchAttributeApi.getBySkuBatchAttributeIds(skuBatchAttributedIds)
                .stream().collect(Collectors.toMap(SkuBatchAttributeDTO::getId, v -> v));

        return operationTaskDTOS.stream().map(v -> {
            SkuBatchAttributeDTO batchAttributeDTO = skuBatchAttributeDTOMap.get(v.getSkuBatchAttributeId());
            SkuMainDataDTO skuMainDataDTO = skuMainDataDTOMap.get(v.getSkuId());
            return new OperationTaskVO().setOperationTaskDTO(v)
                    .setSkuBatchAttributeDTO(batchAttributeDTO)
                    .setSkuMainDataDTO(skuMainDataDTO);
        }).toList();

    }

    public void submitStocktakeRecord(StocktakeRecordSubmitDTO stocktakeRecordsSubmitDTO) {
        stocktakeApi.submitStocktakeRecord(stocktakeRecordsSubmitDTO);
    }

    public void closeStocktakeTasks(Long workStationId) {
        stocktakeApi.closeStocktakeTask(workStationId);
    }
}
