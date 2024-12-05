package org.openwes.wes.stocktake.domain.repository;

import org.openwes.wes.api.stocktake.constants.StocktakeTaskStatusEnum;
import org.openwes.wes.stocktake.domain.entity.StocktakeTask;
import org.openwes.wes.stocktake.domain.entity.StocktakeTaskDetail;

import java.util.Collection;
import java.util.List;

public interface StocktakeTaskRepository {

    void saveOrderAndDetail(StocktakeTask stocktakeTask);

    void saveAllTaskAndDetails(List<StocktakeTask> stocktakeTaskList);

    void saveDetail(StocktakeTaskDetail stocktakeTaskDetail);

    StocktakeTask findById(Long stocktakeTaskId);

    List<StocktakeTask> findAllById(List<Long> stocktakeOrderIds);

    List<StocktakeTask> findAllTasksByWorkStationIdAndStatus(Long workStationId, Collection<StocktakeTaskStatusEnum> statuses);

    List<StocktakeTask> findAllByWorkStationIdAndStatus(Long workStationId, Collection<StocktakeTaskStatusEnum> statuses);

    List<StocktakeTaskDetail> findAllByContainerCodeAndFaceAndStocktakeTaskId(String containerCode, String face, List<Long> stocktakeTaskIds);

    List<StocktakeTask> findAllByStocktakeOrderId(Long stocktakeOrderId);

}
