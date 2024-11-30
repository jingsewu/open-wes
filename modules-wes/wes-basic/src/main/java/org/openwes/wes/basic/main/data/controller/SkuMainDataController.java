package org.openwes.wes.basic.main.data.controller;

import com.google.common.collect.Lists;
import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.main.data.ISkuMainDataApi;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.basic.main.data.domain.entity.SkuMainData;
import org.openwes.wes.basic.main.data.domain.repository.SkuMainDataRepository;
import org.openwes.wes.basic.main.data.transfer.SkuMainDataDTOTransfer;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("basic/sku")
@RequiredArgsConstructor
@Tag(name = "Wms Module Api")
@Validated
public class SkuMainDataController {

    private final ISkuMainDataApi skuMainDataApi;
    private final SkuMainDataRepository skuMainDataRepository;
    private final SkuMainDataDTOTransfer skuMainDataDTOTransfer;

    @PostMapping("createOrUpdate")
    public Object createOrUpdate(@RequestBody @Valid SkuMainDataDTO skuMainDataDTO) {
        skuMainDataApi.createOrUpdateBatch(Lists.newArrayList(skuMainDataDTO));
        return Response.success();
    }

    @PostMapping("{id}")
    public Object getById(@PathVariable Long id) {
        SkuMainData ownerData = skuMainDataRepository.findById(id);
        return skuMainDataDTOTransfer.toDTO(ownerData);
    }
}
