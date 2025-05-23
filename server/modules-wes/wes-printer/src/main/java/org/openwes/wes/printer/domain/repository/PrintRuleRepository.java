package org.openwes.wes.printer.domain.repository;

import org.openwes.wes.printer.domain.entity.PrintRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PrintRuleRepository extends JpaRepository<PrintRule, Long> {

    List<PrintRule> findAllByRuleCodeIn(Collection<String> ruleCodes);
}
