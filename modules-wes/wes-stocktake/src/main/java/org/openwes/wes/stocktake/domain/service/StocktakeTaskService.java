package org.openwes.wes.stocktake.domain.service;

import org.openwes.wes.api.ems.proxy.dto.CreateContainerTaskDTO;
import org.openwes.wes.stocktake.domain.entity.StocktakeTask;
import org.openwes.wes.stocktake.domain.entity.StocktakeTaskDetail;

import java.util.List;

public interface StocktakeTaskService {

    List<CreateContainerTaskDTO> buildContainerTasks(List<StocktakeTaskDetail> details, StocktakeTask stocktakeTask);
}
