package org.openwes.wes.api.basic;

import org.openwes.wes.api.basic.dto.AisleDTO;
import org.openwes.wes.api.basic.dto.LocationDTO;
import org.openwes.wes.api.basic.dto.LocationUpdateDTO;
import jakarta.validation.constraints.NotEmpty;

import java.util.Collection;
import java.util.List;

public interface ILocationApi {

    void createLocations(@NotEmpty List<AisleDTO> aisleDTOS, @NotEmpty List<LocationDTO> locationDTOS);

    void update(LocationUpdateDTO locationUpdateDTO);

    void delete(Long id);

    List<LocationDTO> getByAisle(String aisleCode, Long warehouseAreaId);

    List<LocationDTO> getByShelfCodes(Collection<String> shelfCodes);

    void deleteByAisle(String aisleCode, Long warehouseAreaId);
}
