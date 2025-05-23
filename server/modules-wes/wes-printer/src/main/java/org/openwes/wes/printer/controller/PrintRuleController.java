package org.openwes.wes.printer.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.print.IPrintRuleApi;
import org.openwes.wes.api.print.dto.PrintRuleDTO;
import org.openwes.wes.printer.domain.entity.PrintRule;
import org.openwes.wes.printer.domain.repository.PrintRuleRepository;
import org.openwes.wes.printer.domain.transfer.PrintRuleTransfer;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("print/rule")
@Tag(name = "Wms Module Api")
public class PrintRuleController {

    private final IPrintRuleApi printRuleApi;
    private final PrintRuleRepository printRuleRepository;
    private final PrintRuleTransfer printRuleTransfer;

    @PostMapping("createOrUpdate")
    public Object createOrUpdate(@RequestBody @Valid PrintRuleDTO printRuleDTO) {
        if (printRuleDTO.getId() != null && printRuleDTO.getId() > 0) {
            printRuleApi.updatePrintRule(printRuleDTO);
            return Response.success();
        }
        printRuleApi.createPrintRule(printRuleDTO);
        return Response.success();
    }

    @PostMapping("get/{id}")
    public Object get(@PathVariable Long id) {
        PrintRule printRule = printRuleRepository.findById(id).orElseThrow();
        return printRuleTransfer.toDTO(printRule);
    }

    @PostMapping("delete/{id}")
    public void delete(@PathVariable Long id) {
        printRuleApi.delete(id);
    }

}
