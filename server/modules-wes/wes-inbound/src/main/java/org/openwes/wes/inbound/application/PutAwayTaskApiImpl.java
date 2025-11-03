package org.openwes.wes.inbound.application;

import lombok.RequiredArgsConstructor;
import org.openwes.api.platform.api.constants.CallbackApiTypeEnum;
import org.openwes.wes.api.inbound.IPutAwayTaskApi;
import org.openwes.wes.api.inbound.dto.PutAwayTaskDTO;
import org.openwes.wes.common.facade.CallbackApiFacade;
import org.openwes.wes.inbound.domain.entity.PutAwayTask;
import org.openwes.wes.inbound.domain.repository.PutAwayTaskRepository;
import org.openwes.wes.inbound.domain.service.PutAwayTaskService;
import org.openwes.wes.inbound.domain.transfer.PutAwayTaskTransfer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PutAwayTaskApiImpl implements IPutAwayTaskApi {

    private final PutAwayTaskRepository putAwayTaskRepository;
    private final PutAwayTaskTransfer putAwayTaskTransfer;
    private final PutAwayTaskService putAwayTaskService;
    private final CallbackApiFacade callbackApiFacade;

    @Override
    public void create(List<PutAwayTaskDTO> putAwayTaskDTOs) {
        List<PutAwayTask> putAwayTasks = putAwayTaskTransfer.toDOs(putAwayTaskDTOs);
        putAwayTaskService.validateCreation(putAwayTasks);
        putAwayTaskRepository.saveAllOrdersAndDetails(putAwayTasks);
    }

    @Override
    public void complete(List<Long> putAwayTaskIds, String locationCode) {
        List<PutAwayTask> putAwayTasks = putAwayTaskRepository.findAllByIds(putAwayTaskIds);
        putAwayTasks.forEach(putAwayTask -> putAwayTask.complete(locationCode));

        putAwayTaskRepository.saveAllOrders(putAwayTasks);
        callbackApiFacade.callback(CallbackApiTypeEnum.PUT_AWAY_TASK_COMPLETED, "", putAwayTasks);
    }

}
