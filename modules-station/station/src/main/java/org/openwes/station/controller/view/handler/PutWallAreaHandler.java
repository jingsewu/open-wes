package org.openwes.station.controller.view.handler;

import org.openwes.station.api.vo.WorkStationVO;
import org.openwes.station.controller.view.context.ViewContext;
import org.openwes.station.controller.view.context.ViewHandlerTypeEnum;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.wes.api.basic.constants.PutWallLocationEnum;
import org.openwes.wes.api.basic.dto.WorkStationConfigDTO;
import org.openwes.wes.api.basic.dto.WorkStationDTO;
import org.springframework.stereotype.Service;

@Service
public class PutWallAreaHandler<T extends WorkStationCache> implements IViewHandler<T> {

    @Override
    public void buildView(ViewContext<T> viewContext) {
        WorkStationVO workStationVO = viewContext.getWorkStationVO();
        WorkStationDTO workStationDTO = viewContext.getWorkStationDTO();

        if (workStationDTO.getWorkStationConfig() == null) {
            workStationDTO.setWorkStationConfig(new WorkStationConfigDTO());
        }

        WorkStationConfigDTO.PickingStationConfigDTO pickingStationConfig =
                workStationDTO.getWorkStationConfig().getPickingStationConfig();

        WorkStationVO.PutWallArea putWallArea
                = new WorkStationVO.PutWallArea("merge", pickingStationConfig.getPutWallTagConfig(), workStationDTO.getPutWalls());
        workStationVO.setPutWallArea(putWallArea);
        // hard code by the same of merge put wall display style
        putWallArea.getPutWallViews().forEach(v -> {
            v.setActive(true);

            if (PutWallLocationEnum.LEFT.name().equals(v.getLocation())) {
                v.setDisplayOrder(pickingStationConfig.getLeftPutWallDisplayOrder());
            } else if (PutWallLocationEnum.RIGHT.name().equals(v.getLocation())) {
                v.setDisplayOrder(pickingStationConfig.getRightPutWallDisplayOrder());
            }
        });

        setActivePutWallAndDispatchedPutWallSlots(viewContext);
    }

    @Override
    public ViewHandlerTypeEnum getViewTypeEnum() {
        return ViewHandlerTypeEnum.PUT_WALL_AREA;
    }

    public void setActivePutWallAndDispatchedPutWallSlots(ViewContext<T> viewContext) {

    }
}
