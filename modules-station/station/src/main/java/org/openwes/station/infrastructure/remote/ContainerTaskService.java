package org.openwes.station.infrastructure.remote;

import org.openwes.wes.api.ems.proxy.IContainerTaskApi;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

@Component
public class ContainerTaskService {

    @DubboReference
    private IContainerTaskApi containerTaskApi;

}
