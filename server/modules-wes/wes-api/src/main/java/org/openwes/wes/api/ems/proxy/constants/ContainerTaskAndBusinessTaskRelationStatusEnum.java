package org.openwes.wes.api.ems.proxy.constants;

import java.util.Collection;
import java.util.List;

public enum ContainerTaskAndBusinessTaskRelationStatusEnum {

    NEW,
    /**
     * means that the container task is finished by wms system. like container is picked by operator.
     */
    COMPLETED,
    /**
     * meant that the business task is canceled by wms system
     */
    CANCELED;

    public static final Collection<ContainerTaskAndBusinessTaskRelationStatusEnum> processingStates = List.of(NEW);
}
