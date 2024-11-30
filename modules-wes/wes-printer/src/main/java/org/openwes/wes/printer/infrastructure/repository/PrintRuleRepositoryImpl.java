package org.openwes.wes.printer.infrastructure.repository;

import org.openwes.wes.printer.domain.entity.PrintRule;
import org.openwes.wes.printer.domain.repository.PrintRuleRepository;
import org.openwes.wes.printer.infrastructure.persistence.mapper.PrintRulePORepository;
import org.openwes.wes.printer.infrastructure.persistence.po.PrintRulePO;
import org.openwes.wes.printer.infrastructure.persistence.transfer.PrintRulePOTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrintRuleRepositoryImpl implements PrintRuleRepository {

    private final PrintRulePORepository printRulePORepository;
    private final PrintRulePOTransfer printRulePOTransfer;

    @Override
    public List<PrintRule> findAllByRuleCodeIn(Collection<String> ruleCodes) {
        List<PrintRulePO> printRulePOS = printRulePORepository.findAllByRuleCodeIn(ruleCodes);
        return printRulePOTransfer.toDOs(printRulePOS);
    }
}
