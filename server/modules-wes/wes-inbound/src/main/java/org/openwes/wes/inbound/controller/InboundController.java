package org.openwes.wes.inbound.controller;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.http.Response;
import org.openwes.common.utils.language.core.LanguageContext;
import org.openwes.wes.api.inbound.IInboundPlanOrderApi;
import org.openwes.wes.api.inbound.dto.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("inbound/plan")
@RequiredArgsConstructor
@Schema(description = "入库相关接口")
@Tag(name = "Wms Module Api")
@Validated
public class InboundController {

    private final IInboundPlanOrderApi inboundPlanOrderApi;

    @PostMapping("query/{identifyNo}/{warehouseCode}")
    @Operation(summary = "查询入库计划单", description = "根据传入的 LpnCode 或客户订单号查询入库计划单")
    public Response<InboundPlanOrderDTO> queryByLpnCodeOrCustomerOrderNo(@PathVariable String identifyNo, @PathVariable String warehouseCode) {
        InboundPlanOrderDTO inboundPlanOrderDTO = inboundPlanOrderApi.queryByLpnCodeOrCustomerOrderNoAndThrowException(identifyNo, warehouseCode);
        return Response.success(inboundPlanOrderDTO);
    }

    @PostMapping("accept")
    public void accept(@Valid @RequestBody AcceptRecordDTO acceptRecordDTO) {
        inboundPlanOrderApi.accept(acceptRecordDTO);
    }

    @PostMapping("forceCompleteAccept")
    public void forceCompleteAccept(@RequestParam("inboundPlanOrderId") Long inboundPlanOrderId) {
        inboundPlanOrderApi.forceCompleteAccept(inboundPlanOrderId);
    }

    @PostMapping("/close")
    public void close(@RequestBody Set<Long> inboundPlanOrderIds) {
        inboundPlanOrderApi.close(inboundPlanOrderIds);
    }

    @Operation(summary = "导入入库计划单Excel")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void importOrders(@RequestParam("file") MultipartFile file) throws Exception {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("file can not empty");
        }

        if (!Objects.requireNonNull(file.getOriginalFilename()).endsWith(".xlsx") &&
                !file.getOriginalFilename().endsWith(".xls")) {
            throw new IllegalArgumentException("only support excel file");
        }

        List<ImportInboundPlanOrderBaseDTO> importInboundPlanOrderDTOs;
        if (LanguageContext.getLanguage().equalsIgnoreCase("en")) {
            List<ImportInboundPlanOrderEnDTO> enList = ExcelImportUtil.importExcel(
                    file.getInputStream(), ImportInboundPlanOrderEnDTO.class, new ImportParams());
            importInboundPlanOrderDTOs = MultiLanguageDtoConverter.convertEnListToBase(enList);
        } else {
            List<ImportInboundPlanOrderZhDTO> zhList = ExcelImportUtil.importExcel(
                    file.getInputStream(), ImportInboundPlanOrderZhDTO.class, new ImportParams());
            importInboundPlanOrderDTOs = MultiLanguageDtoConverter.convertZhListToBase(zhList);
        }

        if (importInboundPlanOrderDTOs == null || importInboundPlanOrderDTOs.isEmpty()) {
            throw new IllegalArgumentException("excel file is empty");
        }

        List<InboundPlanOrderDTO> inboundPlanOrderDTOs = convertToInboundPlanOrderDTOs(importInboundPlanOrderDTOs);
        inboundPlanOrderApi.createInboundPlanOrder(inboundPlanOrderDTOs);
    }

    private List<InboundPlanOrderDTO> convertToInboundPlanOrderDTOs(List<ImportInboundPlanOrderBaseDTO> importInboundPlanOrderDTOs) {

        List<InboundPlanOrderDTO> inboundPlanOrderDTOs = new ArrayList<>();

        importInboundPlanOrderDTOs.stream().collect(Collectors.groupingBy(ImportInboundPlanOrderBaseDTO::getCustomerOrderNo))
                .forEach((customerOrderNo, values) -> {
                    ImportInboundPlanOrderBaseDTO importInboundPlanOrderDTO = values.iterator().next();

                    InboundPlanOrderDTO inboundPlanOrderDTO = InboundPlanOrderDTO.build(importInboundPlanOrderDTO);
                    List<InboundPlanOrderDetailDTO> inboundPlanOrderDetailDTOs = values.stream().map(InboundPlanOrderDetailDTO::build).toList();

                    inboundPlanOrderDTO.setDetails(inboundPlanOrderDetailDTOs);
                    inboundPlanOrderDTOs.add(inboundPlanOrderDTO);
                });

        return inboundPlanOrderDTOs;
    }

    @Operation(summary = "下载入库计划单导入模板")
    @PostMapping("/download")
    public ResponseEntity<Resource> downloadTemplate() {

        String language = LanguageContext.getLanguage();
        String templatePath = "templates/inbound-plan-order-template-" + (language.contains("zh") ? "zh" : "en") + ".xlsx";
        ClassPathResource templateResource = new ClassPathResource(templatePath);

        if (!templateResource.exists()) {
            throw new IllegalStateException("inbound plan order template not exists");
        }

        String filename = templateResource.getFilename();
        assert filename != null;
        String encodedFileName = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("\\+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + encodedFileName)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .body(templateResource);
    }

}
