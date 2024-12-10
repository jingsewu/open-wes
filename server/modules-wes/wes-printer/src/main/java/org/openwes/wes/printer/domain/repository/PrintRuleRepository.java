package org.openwes.wes.printer.domain.repository;

import org.openwes.wes.printer.domain.entity.PrintRule;

import java.util.Collection;
import java.util.List;

public interface PrintRuleRepository {

    List<PrintRule> findAllByRuleCodeIn(Collection<String> ruleCodes);
}
