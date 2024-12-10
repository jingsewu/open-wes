package org.openwes.wes.api.basic;

import org.openwes.wes.api.basic.dto.WarehouseAreaGroupDTO;

public interface IWarehouseAreaGroupApi {

    void save(WarehouseAreaGroupDTO warehouseAreaGroupDTO);

    void update(WarehouseAreaGroupDTO warehouseAreaGroupDTO);

    void enable(Long id);

    void disable(Long id);

    void delete(Long id);

}
