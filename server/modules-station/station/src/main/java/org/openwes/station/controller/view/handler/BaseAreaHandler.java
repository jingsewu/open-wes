package org.openwes.station.controller.view.handler;

import org.openwes.station.api.vo.WorkStationVO;
import org.openwes.station.controller.view.context.ViewContext;
import org.openwes.station.controller.view.context.ViewHandlerTypeEnum;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.wes.api.basic.dto.WorkStationDTO;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BaseAreaHandler<T extends WorkStationCache> implements IViewHandler<T> {

    @Override
    public void buildView(ViewContext<T> viewContext) {
        WorkStationVO workStationVO = viewContext.getWorkStationVO();
        WorkStationDTO workStationDTO = viewContext.getWorkStationDTO();
        T workStationCache = viewContext.getWorkStationCache();

        workStationVO.setWorkLocationArea(new WorkStationVO.WorkLocationArea(workStationDTO.getWorkLocations()));
        workStationVO.setWarehouseCode(workStationDTO.getWarehouseCode());
        workStationVO.setStationCode(workStationDTO.getStationCode());
        workStationVO.setStationName(workStationDTO.getStationName());
        workStationVO.setWorkStationStatus(workStationDTO.getWorkStationStatus());
        workStationVO.setWorkStationMode(workStationDTO.getWorkStationMode());
        workStationVO.setWorkStationId(workStationDTO.getId());
        workStationVO.setWarehouseAreaId(String.valueOf(workStationDTO.getWarehouseAreaId()));
        workStationVO.setScanCode(workStationCache.getScannedBarcode());

        workStationVO.setSkuArea(new WorkStationVO.SkuArea());

        setChooseArea(viewContext);
        setToolbar(viewContext);

        setOrderArea(viewContext);

        setStationProcessingStatus(workStationVO, workStationCache);
    }

    protected void setOrderArea(ViewContext<T> viewContext) {

    }

    protected void setStationProcessingStatus(WorkStationVO workStationVO, T workStationCache) {
    }

    @Override
    public ViewHandlerTypeEnum getViewTypeEnum() {
        return ViewHandlerTypeEnum.BASE_AREA;
    }

    protected void setChooseArea(ViewContext<T> viewContext) {
    }

    protected void setToolbar(ViewContext<T> viewContext) {
    }
}
