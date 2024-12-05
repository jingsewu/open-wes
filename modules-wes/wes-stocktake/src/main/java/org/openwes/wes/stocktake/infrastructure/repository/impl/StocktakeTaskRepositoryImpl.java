package org.openwes.wes.stocktake.infrastructure.repository.impl;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.openwes.wes.api.stocktake.constants.StocktakeTaskStatusEnum;
import org.openwes.wes.stocktake.domain.entity.StocktakeTask;
import org.openwes.wes.stocktake.domain.entity.StocktakeTaskDetail;
import org.openwes.wes.stocktake.domain.repository.StocktakeTaskRepository;
import org.openwes.wes.stocktake.infrastructure.persistence.mapper.StocktakeTaskDetailPORepository;
import org.openwes.wes.stocktake.infrastructure.persistence.mapper.StocktakeTaskPORepository;
import org.openwes.wes.stocktake.infrastructure.persistence.po.StocktakeTaskDetailPO;
import org.openwes.wes.stocktake.infrastructure.persistence.po.StocktakeTaskPO;
import org.openwes.wes.stocktake.infrastructure.persistence.transfer.StocktakeTaskPOTransfer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StocktakeTaskRepositoryImpl implements StocktakeTaskRepository {

    private final StocktakeTaskPORepository stocktakeTaskPORepository;
    private final StocktakeTaskDetailPORepository stocktakeTaskDetailPORepository;
    private final StocktakeTaskPOTransfer stocktakeTaskPOTransfer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrderAndDetail(StocktakeTask stocktakeTask) {
        StocktakeTaskPO stocktakeTaskPO = stocktakeTaskPORepository.save(stocktakeTaskPOTransfer.toPO(stocktakeTask));

        List<StocktakeTaskDetailPO> stocktakeTaskDetailPOS = stocktakeTaskPOTransfer.toDetailPOS(stocktakeTask.getDetails());
        stocktakeTaskDetailPOS.forEach(v -> v.setStocktakeTaskId(stocktakeTaskPO.getId()));

        List<StocktakeTaskDetailPO> details = stocktakeTaskDetailPORepository.saveAll(stocktakeTaskDetailPOS);

        stocktakeTaskPOTransfer.toDO(stocktakeTaskPO, details);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAllTaskAndDetails(List<StocktakeTask> stocktakeTaskList) {
        stocktakeTaskList.forEach(this::saveOrderAndDetail);
    }

    @Override
    public StocktakeTask findById(Long stocktakeTaskId) {
        StocktakeTaskPO stocktakeTaskPO = stocktakeTaskPORepository.findById(stocktakeTaskId).orElseThrow();
        List<StocktakeTaskDetailPO> stocktakeTaskDetailPOs = stocktakeTaskDetailPORepository
                .findAllByStocktakeTaskIdIn(Lists.newArrayList(stocktakeTaskPO.getId()));
        return stocktakeTaskPOTransfer.toDO(stocktakeTaskPO, stocktakeTaskDetailPOs);
    }

    @Override
    public List<StocktakeTask> findAllById(List<Long> stocktakeOrderIds) {
        List<StocktakeTaskPO> stocktakeTaskPOs = stocktakeTaskPORepository.findAllById(stocktakeOrderIds);
        List<Long> stocktakeTaskIds = stocktakeTaskPOs.stream().map(StocktakeTaskPO::getId).toList();
        Map<Long, List<StocktakeTaskDetailPO>> detailMap = stocktakeTaskDetailPORepository.findAllByStocktakeTaskIdIn(stocktakeTaskIds)
                .stream().collect(Collectors.groupingBy(StocktakeTaskDetailPO::getStocktakeTaskId));
        return stocktakeTaskPOs.stream().map(v -> stocktakeTaskPOTransfer.toDO(v, detailMap.get(v.getId())))
                .toList();
    }

    @Override
    public List<StocktakeTask> findAllTasksByWorkStationIdAndStatus(Long workStationId, Collection<StocktakeTaskStatusEnum> statuses) {
        List<StocktakeTaskPO> stocktakeTaskPOs = stocktakeTaskPORepository.findAllByWorkStationIdAndStocktakeTaskStatusIn(workStationId, statuses);
        return stocktakeTaskPOTransfer.toDOS(stocktakeTaskPOs);
    }

    @Override
    public List<StocktakeTask> findAllByWorkStationIdAndStatus(Long workStationId, Collection<StocktakeTaskStatusEnum> statuses) {
        List<StocktakeTaskPO> stocktakeTaskPOs = stocktakeTaskPORepository.findAllByWorkStationIdAndStocktakeTaskStatusIn(workStationId, statuses);
        List<Long> stocktakeTaskIds = stocktakeTaskPOs.stream().map(StocktakeTaskPO::getId).toList();
        Map<Long, List<StocktakeTaskDetailPO>> detailMap = stocktakeTaskDetailPORepository.findAllByStocktakeTaskIdIn(stocktakeTaskIds)
                .stream().collect(Collectors.groupingBy(StocktakeTaskDetailPO::getStocktakeTaskId));
        return stocktakeTaskPOs.stream().map(v -> stocktakeTaskPOTransfer.toDO(v, detailMap.get(v.getId())))
                .toList();
    }

    @Override
    public List<StocktakeTaskDetail> findAllByContainerCodeAndFaceAndStocktakeTaskId(String containerCode, String face, List<Long> stocktakeTaskIds) {
        List<StocktakeTaskDetailPO> stocktakeTaskDetails = stocktakeTaskDetailPORepository.findAllByContainerCodeAndContainerFaceAndStocktakeTaskIdIn(containerCode, face, stocktakeTaskIds);
        return stocktakeTaskPOTransfer.toDetailDOS(stocktakeTaskDetails);
    }

    @Override
    public List<StocktakeTask> findAllByStocktakeOrderId(Long stocktakeOrderId) {
        return stocktakeTaskPOTransfer.toDOS(stocktakeTaskPORepository.findAllByStocktakeOrderId(stocktakeOrderId));
    }

    @Override
    public void saveDetail(StocktakeTaskDetail stocktakeTaskDetail) {
        stocktakeTaskDetailPORepository.save(stocktakeTaskPOTransfer.toDetailPO(stocktakeTaskDetail));
    }
}
