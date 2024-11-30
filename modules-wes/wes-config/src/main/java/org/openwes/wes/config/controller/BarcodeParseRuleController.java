package org.openwes.wes.config.controller;

import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.config.IBarcodeParseRuleApi;
import org.openwes.wes.api.config.dto.BarcodeParseRequestDTO;
import org.openwes.wes.api.config.dto.BarcodeParseResult;
import org.openwes.wes.api.config.dto.BarcodeParseRuleDTO;
import org.openwes.wes.config.domain.entity.BarcodeParseRule;
import org.openwes.wes.config.domain.repository.BarcodeParseRuleRepository;
import org.openwes.wes.config.domain.transfer.BarcodeParseRuleTransfer;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("config/barcode/parse")
@Validated
@RequiredArgsConstructor
@Tag(name = "Wms Module Api")
public class BarcodeParseRuleController {

    private final IBarcodeParseRuleApi barcodeParseRuleApi;
    private final BarcodeParseRuleRepository barcodeParseRuleRepository;
    private final BarcodeParseRuleTransfer barcodeParseRuleTransfer;

    @PostMapping("createOrUpdate")
    public Object createOrUpdate(@RequestBody @Valid BarcodeParseRuleDTO barcodeParseRuleDTO) {
        if (barcodeParseRuleDTO.getId() != null && barcodeParseRuleDTO.getId() > 0) {
            barcodeParseRuleApi.update(barcodeParseRuleDTO);
            return Response.success();
        }
        barcodeParseRuleApi.save(barcodeParseRuleDTO);
        return Response.success();
    }

    @PostMapping("get/{id}")
    public Object getById(@PathVariable Long id) {
        BarcodeParseRule barcodeParseRule = barcodeParseRuleRepository.findById(id);
        return Response.builder().data(barcodeParseRuleTransfer.toDTO(barcodeParseRule)).build();
    }

    @PostMapping("parse")
    public Response parse(@RequestBody BarcodeParseRequestDTO request) {
        List<BarcodeParseResult> results = barcodeParseRuleApi.parse(request);
        return Response.success(results);
    }
}
