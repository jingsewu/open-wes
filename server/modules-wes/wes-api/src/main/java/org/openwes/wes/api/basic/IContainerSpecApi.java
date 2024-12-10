package org.openwes.wes.api.basic;

import org.openwes.wes.api.basic.dto.ContainerSpecDTO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

public interface IContainerSpecApi {

    void save(@Valid ContainerSpecDTO containerSpecDTO);

    void update(@Valid ContainerSpecDTO containerSpecDTO);

    ContainerSpecDTO getContainerSpecDTO(String containerSpecCode, String warehouseCode);

    List<ContainerSpecDTO> queryContainerSpec(Collection<String> containerSpecCodes, String warehouseCode);

    void delete(Long id);
}
