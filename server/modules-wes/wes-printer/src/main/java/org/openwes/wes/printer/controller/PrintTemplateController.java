package org.openwes.wes.printer.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.print.IPrintTemplateApi;
import org.openwes.wes.api.print.dto.PrintTemplateDTO;
import org.openwes.wes.printer.domain.entity.PrintRule;
import org.openwes.wes.printer.domain.entity.PrintTemplate;
import org.openwes.wes.printer.domain.repository.PrintTemplateRepository;
import org.openwes.wes.printer.domain.transfer.PrintTemplateTransfer;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("print/template")
@Tag(name = "Wms Module Api")
public class PrintTemplateController {

    private final IPrintTemplateApi printTemplateApi;
    private final PrintTemplateRepository printTemplateRepository;
    private final PrintTemplateTransfer printTemplateTransfer;

    @PostMapping("createOrUpdate")
    public Object createOrUpdate(@RequestBody @Valid PrintTemplateDTO printTemplateDTO) {
        if (printTemplateDTO.getId() != null && printTemplateDTO.getId() > 0) {
            printTemplateApi.updatePrintTemplate(printTemplateDTO);
            return Response.success();
        }
        printTemplateApi.createPrintTemplate(printTemplateDTO);
        return Response.success();
    }

    @PostMapping("get/{id}")
    public Object get(@PathVariable Long id) {
        PrintTemplate printTemplate = printTemplateRepository.findById(id).orElseThrow();
        return printTemplateTransfer.toDTO(printTemplate);
    }

}
