package org.openwes.wes.config.infrastructure.persistence.mapper;

import org.openwes.wes.api.config.constants.BusinessFlowEnum;
import org.openwes.wes.api.config.constants.ExecuteTimeEnum;
import org.openwes.wes.config.infrastructure.persistence.po.BarcodeParseRulePO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BarcodeParseRulePORepository extends JpaRepository<BarcodeParseRulePO, Long> {
    BarcodeParseRulePO findByCode(String barcodeRuleCode);

    List<BarcodeParseRulePO> findByBusinessFlowAndExecuteTime(BusinessFlowEnum businessFlow, ExecuteTimeEnum executeTime);
}
