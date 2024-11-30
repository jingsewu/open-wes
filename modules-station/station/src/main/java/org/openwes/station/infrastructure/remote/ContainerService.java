package org.openwes.station.infrastructure.remote;

import org.openwes.wes.api.basic.IContainerApi;
import org.openwes.wes.api.basic.dto.ContainerDTO;
import org.openwes.wes.api.basic.dto.ContainerSpecDTO;
import lombok.Setter;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.Set;

@Setter
@Service
public class ContainerService {

    @DubboReference
    private IContainerApi containerApi;

    public ContainerSpecDTO queryContainerLayout(String containerCode, String warehouseCode, String face) {
        return containerApi.queryContainerLayout(containerCode, warehouseCode, face);
    }

    public ContainerDTO queryContainer(String containerCode, String warehouseCode) {
        return containerApi.queryContainer(containerCode, warehouseCode);
    }

    public void unLockContainer(String warehouseCode, Set<String> containerCodes) {
        containerApi.unLockContainer(warehouseCode, containerCodes);
    }

}
