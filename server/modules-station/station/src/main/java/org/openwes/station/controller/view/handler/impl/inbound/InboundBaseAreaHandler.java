package org.openwes.station.controller.view.handler.impl.inbound;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.openwes.station.api.vo.WorkStationVO;
import org.openwes.station.controller.view.context.ViewContext;
import org.openwes.station.controller.view.context.ViewHandlerTypeEnum;
import org.openwes.station.controller.view.handler.BaseAreaHandler;
import org.openwes.station.domain.entity.InboundWorkStationCache;
import org.openwes.wes.api.basic.constants.WorkStationProcessingStatusEnum;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InboundBaseAreaHandler extends BaseAreaHandler<InboundWorkStationCache> {

    @Override
    public void setStationProcessingStatus(WorkStationVO workStationVO, InboundWorkStationCache workStationCache) {

        if (ObjectUtils.isEmpty(workStationCache.getCallContainers())) {
            workStationVO.setStationProcessingStatus(WorkStationProcessingStatusEnum.WAIT_CALL_CONTAINER);
            return;
        }

        workStationVO.setCallContainers(workStationCache.getCallContainers());

        if (ObjectUtils.isNotEmpty(workStationCache.getArrivedContainers())) {
            return;
        }

        workStationVO.setStationProcessingStatus(WorkStationProcessingStatusEnum.WAIT_ROBOT);
    }

    @Override
    protected void setToolbar(ViewContext<InboundWorkStationCache> viewContext) {
        super.setToolbar(viewContext);
    }

    @Override
    public ViewHandlerTypeEnum getViewTypeEnum() {
        return ViewHandlerTypeEnum.INBOUND_BASE_AREA;
    }
}
