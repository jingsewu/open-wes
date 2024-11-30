package org.openwes.wes.api.ems.proxy;


import org.openwes.wes.api.ems.proxy.dto.ContainerArrivedEvent;
import org.openwes.wes.api.ems.proxy.dto.ContainerOperation;

public interface IContainerOperatorApi {

    void containerArrive(ContainerArrivedEvent containerArrivedEvent);

    void containerLeave(ContainerOperation containerOperation);
}
