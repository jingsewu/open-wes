package org.openwes.station.application.business.handler.common;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.application.business.handler.IBusinessHandler;
import org.openwes.station.application.business.handler.common.extension.ExtensionFactory;
import org.openwes.station.application.business.handler.common.extension.IExtension;
import org.openwes.station.api.model.ArrivedContainerCache;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.infrastructure.remote.EquipmentService;
import org.openwes.wes.api.ems.proxy.constants.ContainerOperationTypeEnum;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContainerLeaveHandler implements IBusinessHandler<String> {

    private final WorkStationCacheRepository workStationCacheRepository;
    private final EquipmentService equipmentService;
    private final ExtensionFactory extensionFactory;

    @Override
    public void execute(String containerCode, Long workStationId) {

        WorkStationCache workStationCache = workStationCacheRepository.findById(workStationId);

        // Collect containers matching the code before removing
        List<ArrivedContainerCache> leavingContainers = workStationCache.getWorkLocationArea().getAllContainers()
                .stream().filter(c -> c.getContainerCode().equals(containerCode)).toList();

        workStationCache.getWorkLocationArea().removeContainer(containerCode);
        workStationCacheRepository.save(workStationCache);

        equipmentService.containerLeave(leavingContainers, ContainerOperationTypeEnum.LEAVE);

        ContainerLeaveHandler.Extension extension = extensionFactory.getExtension(workStationCache.getWorkStationMode(),
                getApiCode());
        if (extension != null) {
            extension.doAfterContainerLeave(workStationCache, containerCode);
        }

    }

    @Override
    public ApiCodeEnum getApiCode() {
        return ApiCodeEnum.CONTAINER_LEAVE;
    }

    @Override
    public Class<String> getParameterClass() {
        return String.class;
    }

    public interface Extension extends IExtension {
        void doAfterContainerLeave(WorkStationCache workStationCache, String containerCode);

        default ApiCodeEnum getApiCode() {
            return ApiCodeEnum.CONTAINER_LEAVE;
        }
    }

}
