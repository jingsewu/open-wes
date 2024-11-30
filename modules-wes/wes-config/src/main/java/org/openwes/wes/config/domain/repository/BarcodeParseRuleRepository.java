package org.openwes.wes.config.domain.repository;

import org.openwes.wes.api.config.constants.BusinessFlowEnum;
import org.openwes.wes.api.config.constants.ExecuteTimeEnum;
import org.openwes.wes.config.domain.entity.BarcodeParseRule;

import java.util.List;

public interface BarcodeParseRuleRepository {

    void save(BarcodeParseRule toBarcodeParseRule);

    BarcodeParseRule findById(Long barcodeParseRuleId);

    List<BarcodeParseRule> findAllByBusinessFlowAndExecuteTime(BusinessFlowEnum businessFlow, ExecuteTimeEnum executeTime);

}
