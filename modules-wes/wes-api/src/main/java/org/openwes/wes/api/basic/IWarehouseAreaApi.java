package org.openwes.wes.api.basic;

import org.openwes.wes.api.basic.constants.WarehouseAreaWorkTypeEnum;
import org.openwes.wes.api.basic.dto.WarehouseAreaDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Collection;
import java.util.List;

public interface IWarehouseAreaApi {

    void save(@Valid WarehouseAreaDTO warehouseAreaDTO);

    void update(@Valid WarehouseAreaDTO warehouseAreaDTO);

    void enable(@NotNull Long id);

    void disable(@NotNull Long id);

    void delete(@Valid Long id);

    WarehouseAreaDTO getById(Long warehouseAreaId);

    List<WarehouseAreaDTO> getByIds(Collection<Long> warehouseAreaIds);

    List<WarehouseAreaDTO> getByWarehouseCode(String warehouseCode);

    Collection<WarehouseAreaDTO> findByWarehouseAreaWorkType(WarehouseAreaWorkTypeEnum warehouseAreaWorkTypeEnum);
}
