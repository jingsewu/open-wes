package org.openwes.wes.stocktake.domain.service.impl;

import com.google.common.collect.Lists;
import org.openwes.common.utils.id.SnowflakeUtils;
import org.openwes.wes.api.ems.proxy.constants.BusinessTaskTypeEnum;
import org.openwes.wes.api.ems.proxy.constants.ContainerTaskTypeEnum;
import org.openwes.wes.api.ems.proxy.dto.CreateContainerTaskDTO;
import org.openwes.wes.stocktake.domain.entity.StocktakeTask;
import org.openwes.wes.stocktake.domain.entity.StocktakeTaskDetail;
import org.openwes.wes.stocktake.domain.service.StocktakeTaskService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StocktakeTaskServiceImpl implements StocktakeTaskService {

    @Override
    public List<CreateContainerTaskDTO> buildContainerTasks(List<StocktakeTaskDetail> details, StocktakeTask stocktakeTask) {
        return details.stream()
                .map(detail -> new CreateContainerTaskDTO()
                        .setContainerCode(detail.getContainerCode())
                        .setContainerFace(detail.getContainerFace())
                        .setTaskGroupCode(String.valueOf(SnowflakeUtils.generateId()))
                        .setTaskPriority(0)
                        .setDestinations(Lists.newArrayList(String.valueOf(stocktakeTask.getWorkStationId())))
                        .setContainerTaskType(ContainerTaskTypeEnum.OUTBOUND)
                        .setCustomerTaskId(detail.getId())
                        .setBusinessTaskType(BusinessTaskTypeEnum.STOCKTAKE)
                ).toList();
    }

}
