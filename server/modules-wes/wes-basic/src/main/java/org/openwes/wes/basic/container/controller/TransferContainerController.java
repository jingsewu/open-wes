package org.openwes.wes.basic.container.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.openwes.wes.api.basic.ITransferContainerApi;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("basic/transfer/container")
@Validated
@RequiredArgsConstructor
@Tag(name = "Wms Module Api")
public class TransferContainerController {

    private final ITransferContainerApi transferContainerApi;

    @PostMapping("release/{id}")
    public void release(@PathVariable Long id) {
        transferContainerApi.release(id);
    }

}
