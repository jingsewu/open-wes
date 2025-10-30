package org.openwes.wes.basic.main.data.controller;

import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.main.data.ISkuMainDataApi;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.basic.main.data.domain.entity.SkuMainData;
import org.openwes.wes.basic.main.data.domain.repository.SkuMainDataRepository;
import org.openwes.wes.basic.main.data.domain.transfer.SkuMainDataTransfer;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("basic/sku")
@RequiredArgsConstructor
@Tag(name = "Wms Module Api")
@Validated
public class SkuMainDataController {

    private final ISkuMainDataApi skuMainDataApi;
    private final SkuMainDataRepository skuMainDataRepository;
    private final SkuMainDataTransfer skuMainDataTransfer;

    @PostMapping("createOrUpdate")
    public Object createOrUpdate(@RequestBody @Valid SkuMainDataDTO skuMainDataDTO) {
        skuMainDataApi.createOrUpdateBatch(Lists.newArrayList(skuMainDataDTO));
        return Response.success();
    }

    @PostMapping("getById/{id}")
    public Object getById(@PathVariable Long id) {
        SkuMainData ownerData = skuMainDataRepository.findById(id);
        return skuMainDataTransfer.toDTO(ownerData);
    }

    @PostMapping("getBySkuCode")
    public Object getBySkuCode(@RequestParam("skuCode") String skuCode) {
        List<SkuMainData> skus = skuMainDataRepository.findAllBySkuCode(skuCode);
        return skuMainDataTransfer.toDTOs(skus);
    }
}
