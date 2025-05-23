package org.openwes.wes.printer.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.print.IPrintConfigApi;
import org.openwes.wes.api.print.dto.PrintConfigDTO;
import org.openwes.wes.printer.domain.entity.PrintConfig;
import org.openwes.wes.printer.domain.repository.PrintConfigRepository;
import org.openwes.wes.printer.domain.transfer.PrintConfigTransfer;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("print/config")
@Tag(name = "Wms Module Api")
public class PrintConfigController {

    private final IPrintConfigApi printConfigApi;
    private final PrintConfigRepository printConfigRepository;
    private final PrintConfigTransfer printConfigTransfer;

    @PostMapping("createOrUpdate")
    public Object createOrUpdate(@RequestBody @Valid PrintConfigDTO printConfigDTO) {
        if (printConfigDTO.getId() != null && printConfigDTO.getId() > 0) {
            printConfigApi.updatePrintConfig(printConfigDTO);
            return Response.success();
        }
        printConfigApi.createPrintConfig(printConfigDTO);
        return Response.success();
    }

    @PostMapping("get/{id}")
    public Object get(@PathVariable Long id) {
        PrintConfig printConfig = printConfigRepository.findById(id).orElseThrow();
        return printConfigTransfer.toDTO(printConfig);
    }

    @PostMapping("delete/{id}")
    public void delete(@PathVariable Long id) {
        printConfigApi.delete(id);
    }

}
