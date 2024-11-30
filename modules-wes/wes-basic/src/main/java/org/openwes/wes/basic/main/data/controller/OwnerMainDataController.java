package org.openwes.wes.basic.main.data.controller;

import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.main.data.IOwnerMainDataApi;
import org.openwes.wes.api.main.data.dto.OwnerMainDataDTO;
import org.openwes.wes.basic.main.data.domain.entity.OwnerMainData;
import org.openwes.wes.basic.main.data.domain.repository.OwnerMainDataRepository;
import org.openwes.wes.basic.main.data.domain.transfer.OwnerMainDataTransfer;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("basic/owner")
@Tag(name = "Wms Module Api")
public class OwnerMainDataController {

    private final IOwnerMainDataApi ownerApi;
    private final OwnerMainDataRepository ownerDataRepository;
    private final OwnerMainDataTransfer ownerDataTransfer;

    @PostMapping("createOrUpdate")
    public Object createOrUpdate(@RequestBody @Valid OwnerMainDataDTO ownerDataDTO) {
        if (ownerDataDTO.getId() != null && ownerDataDTO.getId() > 0) {
            ownerApi.updateOwner(ownerDataDTO);
            return Response.success();
        }
        ownerApi.createOwner(ownerDataDTO);
        return Response.success();
    }

    @PostMapping("{id}")
    public Object getById(@PathVariable Long id) {
        OwnerMainData skuMainData = ownerDataRepository.findById(id);
        return ownerDataTransfer.toDTO(skuMainData);
    }
}
