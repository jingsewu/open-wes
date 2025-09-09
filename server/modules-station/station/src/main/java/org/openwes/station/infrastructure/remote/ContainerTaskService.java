package org.openwes.station.infrastructure.remote;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.BasicErrorDescEnum;
import org.openwes.common.utils.id.IdGenerator;
import org.openwes.station.domain.entity.InboundWorkStationCache;
import org.openwes.wes.api.basic.IContainerApi;
import org.openwes.wes.api.basic.dto.ContainerDTO;
import org.openwes.wes.api.ems.proxy.IContainerTaskApi;
import org.openwes.wes.api.ems.proxy.constants.BusinessTaskTypeEnum;
import org.openwes.wes.api.ems.proxy.constants.ContainerTaskTypeEnum;
import org.openwes.wes.api.ems.proxy.dto.ContainerTaskDTO;
import org.openwes.wes.api.ems.proxy.dto.CreateContainerTaskDTO;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.openwes.common.utils.exception.code_enum.StationErrorDescEnum.CONTAINER_HAS_NOT_EMPTY_SLOT;

@Component
public class ContainerTaskService {

    @DubboReference
    private IContainerTaskApi containerTaskApi;

    @DubboReference
    private IContainerApi containerApi;

    public List<ContainerTaskDTO> createContainerTasks(List<String> containerCodes, InboundWorkStationCache workStationCache) {

        Long customerTaskId = IdGenerator.generateId();

        Collection<ContainerDTO> containers = containerApi.queryContainer(containerCodes, workStationCache.getWarehouseCode());
        if (ObjectUtils.isEmpty(containers)) {
            throw WmsException.throwWmsException(BasicErrorDescEnum.CONTAINER_NOT_EXIST, containerCodes);
        }
        Map<ContainerDTO, Set<String>> containerMap = containers.stream().collect(Collectors.toMap(v -> v, ContainerDTO::getEmptySlotFaces));

        if (ObjectUtils.isEmpty(containerMap) || containerMap.values().stream().filter(ObjectUtils::isNotEmpty)
                .flatMap(Collection::stream).toList().isEmpty()) {
            throw WmsException.throwWmsException(CONTAINER_HAS_NOT_EMPTY_SLOT, containerCodes);
        }

        List<CreateContainerTaskDTO> containerTaskDTOS = Lists.newArrayList();
        containerMap.forEach((container, faces) -> {
            if (ObjectUtils.isEmpty(faces)) {
                return;
            }
            containerTaskDTOS.addAll(faces.stream().map(face -> {
                CreateContainerTaskDTO createContainerTaskDTO = new CreateContainerTaskDTO();
                createContainerTaskDTO.setTaskPriority(0);
                createContainerTaskDTO.setTaskGroupPriority(0);
                createContainerTaskDTO.setCustomerTaskId(customerTaskId);
                createContainerTaskDTO.setBusinessTaskType(BusinessTaskTypeEnum.SELECT_CONTAINER_PUT_AWAY);
                createContainerTaskDTO.setContainerTaskType(ContainerTaskTypeEnum.OUTBOUND);
                createContainerTaskDTO.setTaskGroupCode(StringUtils.EMPTY);
                createContainerTaskDTO.setContainerCode(container.getContainerCode());
                createContainerTaskDTO.setContainerFace(face);
                createContainerTaskDTO.setDestinations(Lists.newArrayList(String.valueOf(workStationCache.getId())));
                return createContainerTaskDTO;
            }).toList());
        });

        return containerTaskApi.createContainerTasks(containerTaskDTOS);
    }

    public void cancel(List<String> taskCodes) {
        containerTaskApi.cancel(taskCodes);
    }
}
