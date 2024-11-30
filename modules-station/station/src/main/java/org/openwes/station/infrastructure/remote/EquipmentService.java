package org.openwes.station.infrastructure.remote;

import org.openwes.api.platform.api.ICallbackApi;
import org.openwes.api.platform.api.constants.CallbackApiTypeEnum;
import org.openwes.api.platform.api.dto.callback.CallbackMessage;
import org.openwes.station.domain.entity.ArrivedContainerCache;
import org.openwes.wes.api.ems.proxy.IContainerOperatorApi;
import org.openwes.wes.api.ems.proxy.constants.ContainerOperationTypeEnum;
import org.openwes.wes.api.ems.proxy.dto.ContainerArrivedEvent;
import org.openwes.wes.api.ems.proxy.dto.ContainerOperation;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class EquipmentService {

    @DubboReference
    private ICallbackApi callbackApi;
    @DubboReference
    private IContainerOperatorApi containerOperatorApi;

    public void containerLeave(Collection<ArrivedContainerCache> arrivedContainers, ContainerOperationTypeEnum containerLeaveType) {

        List<ContainerOperation.ContainerOperationDetail> containerOperationDetails = arrivedContainers
                .stream().map(v -> new ContainerOperation.ContainerOperationDetail()
                        .setContainerCode(v.getContainerCode())
                        .setContainerFace(v.getFace())
                        .setTaskCode(ObjectUtils.isEmpty(v.getTaskCodes()) ? "" : v.getTaskCodes().iterator().next())
                        .setLocationCode(v.getLocationCode())
                        .setOperationType(containerLeaveType))
                .toList();

        ArrivedContainerCache next = arrivedContainers.iterator().next();
        ContainerOperation containerOperation = new ContainerOperation().setWorkStationId(next.getWorkStationId()).setGroupCode(next.getGroupCode())
                .setContainerOperationDetails(containerOperationDetails);
        containerOperatorApi.containerLeave(containerOperation);
    }


    public void containerLeave(ContainerArrivedEvent containerArrivedEvent) {
        List<ContainerOperation.ContainerOperationDetail> containerOperationDetails = containerArrivedEvent.getContainerDetails()
                .stream().map(v -> new ContainerOperation.ContainerOperationDetail()
                        .setContainerCode(v.getContainerCode())
                        .setContainerFace(v.getFace())
                        .setTaskCode(ObjectUtils.isEmpty(v.getTaskCodes()) ? "" : v.getTaskCodes().iterator().next())
                        .setLocationCode(v.getLocationCode())
                        .setOperationType(ContainerOperationTypeEnum.LEAVE))
                .toList();

        ContainerArrivedEvent.ContainerDetail containerDetail = containerArrivedEvent.getContainerDetails().get(0);
        ContainerOperation containerOperation = new ContainerOperation()
                .setWorkStationId(containerArrivedEvent.getWorkStationId()).setGroupCode(containerDetail.getGroupCode())
                .setContainerOperationDetails(containerOperationDetails);

        containerOperatorApi.containerLeave(containerOperation);
    }

    public void callRobot(Long workStationId) {
        callbackApi.callback(CallbackApiTypeEnum.CALL_ROBOT, null, new CallbackMessage<>().setData(workStationId));
    }

}
