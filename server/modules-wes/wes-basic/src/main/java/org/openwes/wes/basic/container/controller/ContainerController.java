package org.openwes.wes.basic.container.controller;

import org.openwes.wes.api.basic.IContainerApi;
import org.openwes.wes.api.basic.dto.BatchCreateContainerDTO;
import org.openwes.wes.api.basic.dto.ContainerDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("basic/container")
@Validated
@RequiredArgsConstructor
@Tag(name = "Wms Module Api")
public class ContainerController {

    private final IContainerApi containerApi;

    @PostMapping("create")
    public void create(@RequestBody BatchCreateContainerDTO createContainerDTO) {
        containerApi.batchCreateContainer(createContainerDTO);
    }

    @PostMapping("changeContainerSpec/{containerId}")
    public void changeContainerSpec(@PathVariable("containerId") Long containerId,
                                    @RequestParam("containerSpecCode") String containerSpecCode) {
        containerApi.changeContainerSpec(containerId, containerSpecCode);
    }

    @PostMapping("get")
    public ContainerDTO get(@RequestParam("containerCode") String containerCode,
                            @RequestParam("warehouseCode") String warehouseCode) {
        return containerApi.queryContainer(containerCode, warehouseCode);
    }
}
