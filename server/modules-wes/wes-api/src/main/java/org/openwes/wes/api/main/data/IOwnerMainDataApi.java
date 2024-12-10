package org.openwes.wes.api.main.data;

import org.openwes.wes.api.main.data.dto.OwnerMainDataDTO;
import jakarta.validation.Valid;

import java.util.Collection;

public interface IOwnerMainDataApi {

    void createOwner(@Valid OwnerMainDataDTO ownerMainDataDTO);

    void updateOwner(@Valid OwnerMainDataDTO ownerMainDataDTO);

    Collection<OwnerMainDataDTO> getOwners(Collection<String> ownerCodes, String warehouseCode);
}
