package org.openwes.wes.config.controller;

import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.config.IBatchAttributeConfigApi;
import org.openwes.wes.api.config.dto.BatchAttributeConfigDTO;
import org.openwes.wes.config.domain.entity.BatchAttributeConfig;
import org.openwes.wes.config.domain.repository.BatchAttributeConfigRepository;
import org.openwes.wes.config.domain.transfer.BatchAttributeConfigTransfer;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("config/batchAttribute")
@Validated
@RequiredArgsConstructor
@Tag(name = "Wms Module Api")
public class BatchAttributeConfigController {

    private final IBatchAttributeConfigApi batchAttributeConfigApi;
    private final BatchAttributeConfigRepository batchAttributeConfigRepository;
    private final BatchAttributeConfigTransfer batchAttributeConfigTransfer;

    @PostMapping("createOrUpdate")
    public Object createOrUpdate(@RequestBody @Valid BatchAttributeConfigDTO batchAttributeConfigDTO) {
        if (batchAttributeConfigDTO.getId() != null && batchAttributeConfigDTO.getId() > 0) {
            batchAttributeConfigApi.update(batchAttributeConfigDTO);
            return Response.success();
        }
        batchAttributeConfigApi.save(batchAttributeConfigDTO);
        return Response.success();
    }

    @PostMapping("get/{id}")
    public Object getById(@PathVariable Long id) {
        BatchAttributeConfig batchAttributeConfig = batchAttributeConfigRepository.findById(id);
        return batchAttributeConfigTransfer.toDTO(batchAttributeConfig);
    }
}
