package org.openwes.wes.ems.proxy.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.openwes.api.platform.api.constants.CallbackApiTypeEnum;
import org.openwes.plugin.sdk.utils.LifecycleListenerRegistry;
import org.openwes.wes.api.ems.proxy.IContainerTaskApi;
import org.openwes.wes.api.ems.proxy.constants.BusinessTaskTypeEnum;
import org.openwes.wes.api.ems.proxy.constants.ContainerTaskStatusEnum;
import org.openwes.wes.api.ems.proxy.constants.ContainerTaskTypeEnum;
import org.openwes.wes.api.ems.proxy.dto.CreateContainerTaskDTO;
import org.openwes.wes.api.ems.proxy.dto.UpdateContainerTaskDTO;
import org.openwes.wes.common.facade.CallbackApiFacade;
import org.openwes.wes.ems.proxy.domain.entity.ContainerTask;
import org.openwes.wes.ems.proxy.domain.repository.ContainerTaskRepository;
import org.openwes.wes.ems.proxy.domain.service.ContainerTaskService;
import org.openwes.wes.ems.proxy.domain.transfer.ContainerTaskTransfer;
import org.openwes.wes.ems.proxy.infrastructure.remote.WmsTaskCallbackFacade;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Primary
@Service
@DubboService
@RequiredArgsConstructor
@Validated
public class ContainerTaskApiImpl implements IContainerTaskApi {

    private final ContainerTaskService containerTaskService;
    private final ContainerTaskTransfer containerTaskTransfer;
    private final ContainerTaskRepository containerTaskRepository;
    private final CallbackApiFacade callbackApiFacade;
    private final WmsTaskCallbackFacade wmsTaskCallbackFacade;
    private final LifecycleListenerRegistry lifecycleListenerRegistry;

    @Override
    public void createContainerTasks(List<CreateContainerTaskDTO> createContainerTasks) {

        lifecycleListenerRegistry.fireBeforeCreate(ContainerTask.class.getSimpleName(), createContainerTasks);

        List<ContainerTask> containerTasks = containerTaskService.groupContainerTasks(createContainerTasks);
        List<ContainerTask> flatContainerTasks = containerTaskService.flatContainerTasks(containerTasks);

        containerTaskRepository.saveAll(flatContainerTasks);

        ContainerTaskTypeEnum containerTaskType = createContainerTasks.iterator().next().getContainerTaskType();

        callbackApiFacade.callback(CallbackApiTypeEnum.CONTAINER_TASK_CREATE, containerTasks, containerTaskType);

        lifecycleListenerRegistry.fireAfterCreate(ContainerTask.class.getSimpleName(), containerTaskTransfer.toDTOs(flatContainerTasks));
    }

    @Override
    public void updateContainerTaskStatus(List<UpdateContainerTaskDTO> updateContainerTasks) {
        List<ContainerTask> containerTasks = containerTaskRepository.findAllByTaskCodes(
                updateContainerTasks.stream().map(UpdateContainerTaskDTO::getTaskCode).toList());

        Map<String, UpdateContainerTaskDTO> taskCodeMap = updateContainerTasks.stream()
                .collect(Collectors.toMap(UpdateContainerTaskDTO::getTaskCode, v -> v));

        containerTasks.forEach(containerTask -> {
            UpdateContainerTaskDTO updateContainerTaskDTO = taskCodeMap.get(containerTask.getTaskCode());
            if (updateContainerTaskDTO == null) {
                log.error("task code: {} is not exist.", containerTask.getTaskCode());
                return;
            }

            containerTask.updateTaskStatus(updateContainerTaskDTO.getTaskStatus(), updateContainerTaskDTO.getLocationCode());
        });

        containerTaskRepository.saveAll(containerTasks);

        containerTasks.forEach(containerTask ->
                lifecycleListenerRegistry.fireAfterStatusChange(ContainerTask.class.getSimpleName(),
                        containerTaskTransfer.toDTO(containerTask), containerTask.getTaskStatus().name()));

        containerTaskService.doAfterFinishContainerTasks(containerTasks);

        Map<BusinessTaskTypeEnum, List<ContainerTask>> goupByBusinessTaskTypeMap = containerTasks.stream()
                .filter(v -> v.getTaskStatus() == ContainerTaskStatusEnum.COMPLETED)
                .filter(v -> ObjectUtils.isNotEmpty(v.getCustomerTaskIds()))
                .collect(Collectors.groupingBy(ContainerTask::getBusinessTaskType));

        if (ObjectUtils.isEmpty(goupByBusinessTaskTypeMap)) {
            return;
        }

        goupByBusinessTaskTypeMap.forEach((businessTaskType, doneContainerTasks) ->
                wmsTaskCallbackFacade.wmsTaskCallback(doneContainerTasks, businessTaskType));
    }

    @Override
    public void cancel(Collection<String> taskCodes) {
        List<ContainerTask> containerTasks = containerTaskRepository.findAllByTaskCodes(taskCodes);
        cancelContainerTasks(containerTasks);
    }

    @Override
    public void cancel(List<Long> customerTaskIds) {
        List<ContainerTask> containerTasks = containerTaskRepository.findAllByCustomerTaskIds(customerTaskIds);
        cancelContainerTasks(containerTasks);
    }

    private void cancelContainerTasks(List<ContainerTask> containerTasks) {
        if (containerTasks.isEmpty()) {
            return;
        }

        containerTasks.forEach(containerTask ->
                lifecycleListenerRegistry.fireBeforeCancel(ContainerTask.class.getSimpleName(), containerTaskTransfer.toDTO(containerTask)));

        List<ContainerTask> canceledContainerTasks = containerTasks.stream().filter(ContainerTask::cancel).toList();

        if (CollectionUtils.isEmpty(canceledContainerTasks)) {
            return;
        }

        containerTaskRepository.saveAll(canceledContainerTasks);

        ContainerTask containerTask = canceledContainerTasks.iterator().next();

        callbackApiFacade.callback(CallbackApiTypeEnum.CONTAINER_TASK_CANCEL, canceledContainerTasks, containerTask.getContainerTaskType());

        canceledContainerTasks.forEach(v ->
                lifecycleListenerRegistry.fireAfterCancel(ContainerTask.class.getSimpleName(), containerTaskTransfer.toDTO(v)));
    }

}
