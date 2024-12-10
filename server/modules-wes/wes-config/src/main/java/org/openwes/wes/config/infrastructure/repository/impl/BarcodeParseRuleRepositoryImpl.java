package org.openwes.wes.config.infrastructure.repository.impl;

import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.wes.api.config.constants.BusinessFlowEnum;
import org.openwes.wes.api.config.constants.ExecuteTimeEnum;
import org.openwes.wes.config.domain.entity.BarcodeParseRule;
import org.openwes.wes.config.domain.repository.BarcodeParseRuleRepository;
import org.openwes.wes.config.infrastructure.persistence.mapper.BarcodeParseRulePORepository;
import org.openwes.wes.config.infrastructure.persistence.transfer.BarcodeParseRulePOTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BarcodeParseRuleRepositoryImpl implements BarcodeParseRuleRepository {

    private final BarcodeParseRulePORepository barcodeParseRulePORepository;
    private final BarcodeParseRulePOTransfer barcodeParseRulePOTransfer;

    @Override
    @CacheEvict(cacheNames = RedisConstants.BARCODE_PARSE_RULE_CACHE, key = "#barcodeParseRule.code")
    public void save(BarcodeParseRule barcodeParseRule) {
        barcodeParseRulePORepository.save(barcodeParseRulePOTransfer.toPO(barcodeParseRule));
    }

    @Override
    public BarcodeParseRule findById(Long barcodeParseRuleId) {
        return barcodeParseRulePOTransfer.toDO(barcodeParseRulePORepository.findById(barcodeParseRuleId).orElseThrow());
    }

    @Override
    public List<BarcodeParseRule> findAllByBusinessFlowAndExecuteTime(BusinessFlowEnum businessFlow, ExecuteTimeEnum executeTime) {
        return barcodeParseRulePOTransfer.toDOs(barcodeParseRulePORepository.findByBusinessFlowAndExecuteTime(businessFlow, executeTime));
    }
}
