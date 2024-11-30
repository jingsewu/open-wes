package org.openwes.wes.basic.container.controller;

import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.basic.IContainerSpecApi;
import org.openwes.wes.api.basic.dto.ContainerSpecDTO;
import org.openwes.wes.basic.container.domain.repository.ContainerSpecRepository;
import org.openwes.wes.basic.container.domain.transfer.ContainerSpecTransfer;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("basic/containerSpec")
@Validated
@Tag(name = "Wms Module Api")
@RequiredArgsConstructor
public class ContainerSpecController {

    private final IContainerSpecApi containerSpecApi;
    private final ContainerSpecRepository containerSpecRepository;
    private final ContainerSpecTransfer containerSpecTransfer;

    @PostMapping("createOrUpdate")
    public Object createOrUpdate(@RequestBody @Valid ContainerSpecDTO containerSpecDTO) {
        if (containerSpecDTO.getId() != null && containerSpecDTO.getId() > 0) {
            containerSpecApi.update(containerSpecDTO);
            return Response.success();
        }
        containerSpecApi.save(containerSpecDTO);
        return Response.success();
    }

    @PostMapping("get/{id}")
    public Object getById(@PathVariable Long id) {
        return containerSpecTransfer.toDTO(containerSpecRepository.findById(id));
    }

    @PostMapping("get")
    public Object get(@RequestParam("containerSpecCode") String containerSpecCode,
                      @RequestParam("warehouseCode") String warehouseCode) {
        return containerSpecApi.getContainerSpecDTO(containerSpecCode, warehouseCode);
    }

    @PostMapping("delete/{id}")
    public void delete(@PathVariable Long id) {
        containerSpecApi.delete(id);
    }

}
