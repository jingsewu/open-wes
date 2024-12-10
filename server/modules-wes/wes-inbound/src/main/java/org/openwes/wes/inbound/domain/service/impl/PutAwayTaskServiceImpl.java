package org.openwes.wes.inbound.domain.service.impl;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.BasicErrorDescEnum;
import org.openwes.wes.api.basic.IWarehouseAreaApi;
import org.openwes.wes.api.basic.dto.WarehouseAreaDTO;
import org.openwes.wes.inbound.domain.entity.PutAwayTask;
import org.openwes.wes.inbound.domain.service.PutAwayTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.openwes.wes.api.basic.constants.WarehouseAreaTypeEnum.STORAGE_AREA;

@Service
@RequiredArgsConstructor
public class PutAwayTaskServiceImpl implements PutAwayTaskService {

    private final IWarehouseAreaApi warehouseAreaApi;

    @Override
    public void validateCreation(List<PutAwayTask> putAwayTasks) {
        //TODO validate creating put away tasks
    }

    @Override
    public void calculateDirection(List<PutAwayTask> putAwayTasks) {
        //TODO call put away rule

        String warehouseCode = putAwayTasks.iterator().next().getWarehouseCode();
        List<WarehouseAreaDTO> warehouseAreaDTOS = warehouseAreaApi.getByWarehouseCode(warehouseCode);
        WarehouseAreaDTO warehouseAreaDTO = warehouseAreaDTOS.stream().filter(v -> v.getWarehouseAreaType() == STORAGE_AREA)
                .findFirst().orElseThrow(() -> WmsException.throwWmsException(BasicErrorDescEnum.WAREHOUSE_STORAGE_AREA_NOT_FOUND));

        putAwayTasks.forEach(v -> v.setWarehouseAreaId(warehouseAreaDTO.getId()));

    }
}
