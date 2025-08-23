package org.openwes.station.application.business.handler.common;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.application.business.handler.IBusinessHandler;
import org.openwes.station.application.business.handler.common.extension.ExtensionFactory;
import org.openwes.station.application.business.handler.common.extension.IExtension;
import org.openwes.station.domain.entity.ArrivedContainerCache;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.infrastructure.remote.EquipmentService;
import org.openwes.wes.api.ems.proxy.constants.ContainerOperationTypeEnum;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContainerLeaveHandler<T extends WorkStationCache> implements IBusinessHandler<String> {

    private final WorkStationCacheRepository<T> workStationCacheRepository;
    private final EquipmentService equipmentService;
    private final ExtensionFactory extensionFactory;

    @Override
    public void execute(String containerCode, Long workStationId) {

        T workStationCache = workStationCacheRepository.findById(workStationId);
        List<ArrivedContainerCache> arrivedContainers = workStationCache.clearArrivedContainers(Lists.newArrayList(containerCode));
        workStationCacheRepository.save(workStationCache);

        equipmentService.containerLeave(arrivedContainers, ContainerOperationTypeEnum.LEAVE);

        ContainerLeaveHandler.Extension<T> extension = extensionFactory.getExtension(workStationCache.getWorkStationMode(),
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

    public interface Extension<T extends WorkStationCache> extends IExtension {
        void doAfterContainerLeave(T workStationCache, String containerCode);

        default ApiCodeEnum getApiCode() {
            return ApiCodeEnum.CONTAINER_LEAVE;
        }
    }

}
