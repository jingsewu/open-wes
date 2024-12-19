package org.openwes.wes.task.domain.service;

import org.openwes.wes.api.task.dto.HandleTaskDTO;
import org.openwes.wes.api.task.dto.ReportAbnormalDTO;
import org.openwes.wes.task.domain.entity.OperationTask;

import java.util.List;

public interface OperationTaskService {

    void handleTasks(List<OperationTask> operationTasks, HandleTaskDTO handleTaskDTO);

    void handleAbnormalTasks(List<OperationTask> operationTasks, ReportAbnormalDTO handleTaskDTO);

    void checkUnbindable(Long transferContainerRecordId);

}
