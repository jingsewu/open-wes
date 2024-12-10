package org.openwes.wes.printer.infrastructure.persistence.mapper;

import org.openwes.wes.printer.infrastructure.persistence.po.PrintRulePO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PrintRulePORepository extends JpaRepository<PrintRulePO, Long> {

    List<PrintRulePO> findAllByRuleCodeIn(Collection<String> ruleCodes);
}
