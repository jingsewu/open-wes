package org.openwes.wes.printer.application;

import lombok.RequiredArgsConstructor;
import org.openwes.wes.api.print.IPrintRuleApi;
import org.openwes.wes.api.print.dto.PrintRuleDTO;
import org.openwes.wes.printer.domain.entity.PrintRule;
import org.openwes.wes.printer.domain.repository.PrintRuleRepository;
import org.openwes.wes.printer.domain.transfer.PrintRuleTransfer;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
public class PrintRuleApiImpl implements IPrintRuleApi {

    private final PrintRuleRepository printRuleRepository;
    private final PrintRuleTransfer printRuleTransfer;

    @Override
    public void updatePrintRule(PrintRuleDTO printRuleDTO) {
        printRuleRepository.save(printRuleTransfer.toDO(printRuleDTO));
    }

    @Override
    public void createPrintRule(PrintRuleDTO printRuleDTO) {
        printRuleRepository.save(printRuleTransfer.toDO(printRuleDTO));
    }

    @Override
    public void delete(Long id) {
        PrintRule printRule = printRuleRepository.findById(id).orElseThrow();
        printRule.delete();
        printRuleRepository.save(printRule);
    }
}
