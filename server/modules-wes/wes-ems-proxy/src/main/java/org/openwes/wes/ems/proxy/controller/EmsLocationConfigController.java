package org.openwes.wes.ems.proxy.controller;

import org.openwes.wes.ems.proxy.domain.repository.EmsLocationConfigRepository;
import org.openwes.wes.ems.proxy.domain.transfer.EmsLocationConfigTransfer;
import org.openwes.wes.api.ems.proxy.dto.EmsLocationConfigDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("ems/work/location")
@RequiredArgsConstructor
@Tag(name = "Wms Module Api")
public class EmsLocationConfigController {

    private final EmsLocationConfigRepository emsLocationConfigRepository;
    private final EmsLocationConfigTransfer locationTransfer;

    @PostMapping("createOrUpdate")
    public void createOrUpdate(@Valid @RequestBody EmsLocationConfigDTO emsLocationConfigDTO) {
        emsLocationConfigRepository.save(locationTransfer.toDO(emsLocationConfigDTO));
    }

}
