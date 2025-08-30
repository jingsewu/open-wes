package org.openwes.wes.config.application;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.distribute.lock.DistributeLock;
import org.openwes.wes.api.config.IBarcodeParseRuleApi;
import org.openwes.wes.api.config.constants.ExecuteTimeEnum;
import org.openwes.wes.api.config.constants.ParserObjectEnum;
import org.openwes.wes.api.config.dto.BarcodeParseRequestDTO;
import org.openwes.wes.api.config.dto.BarcodeParseResult;
import org.openwes.wes.api.config.dto.BarcodeParseRuleDTO;
import org.openwes.wes.api.main.data.ISkuMainDataApi;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.config.domain.entity.BarcodeParseRule;
import org.openwes.wes.config.domain.repository.BarcodeParseRuleRepository;
import org.openwes.wes.config.domain.transfer.BarcodeParseRuleTransfer;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.openwes.common.utils.constants.RedisConstants.BARCODE_PARSE_RULE_ADD_LOCK;
import static org.openwes.common.utils.exception.code_enum.BasicErrorDescEnum.*;

@Validated
@Service
@RequiredArgsConstructor
@DubboService
public class BarcodeParseRuleApiImpl implements IBarcodeParseRuleApi {

    private final BarcodeParseRuleRepository barcodeRuleRepository;
    private final BarcodeParseRuleTransfer barcodeParseRuleTransfer;
    private final ISkuMainDataApi skuMainDataApi;
    private final DistributeLock distributeLock;

    @Override
    public void save(BarcodeParseRuleDTO barcodeParseRuleDTO) {
        distributeLock.acquireLockIfThrows(BARCODE_PARSE_RULE_ADD_LOCK, 1000);
        try {
            List<BarcodeParseRule> barcodeParseRules = barcodeRuleRepository
                    .findAllByBusinessFlowAndExecuteTime(barcodeParseRuleDTO.getBusinessFlow(), barcodeParseRuleDTO.getExecuteTime());
            if (barcodeParseRules.stream().anyMatch(barcodeParseRule ->
                    barcodeParseRule.match(barcodeParseRuleDTO.getOwnerCode(), barcodeParseRuleDTO.getBrand()))) {
                throw new WmsException(BARCODE_PARSE_RULE_REPEAT);
            }

            barcodeRuleRepository.save(barcodeParseRuleTransfer.toDO(barcodeParseRuleDTO));
        } finally {
            distributeLock.releaseLock(BARCODE_PARSE_RULE_ADD_LOCK);
        }

    }

    @Override
    public void update(BarcodeParseRuleDTO barcodeParseRuleDTO) {

        distributeLock.acquireLockIfThrows(BARCODE_PARSE_RULE_ADD_LOCK, 1000);
        try {
            BarcodeParseRule barcodeParseRule = barcodeRuleRepository.findById(barcodeParseRuleDTO.getId());
            if (!StringUtils.equals(barcodeParseRule.getCode(), barcodeParseRuleDTO.getCode())) {
                throw new WmsException(CODE_MUST_NOT_UPDATE);
            }
            Preconditions.checkState(Objects.equals(barcodeParseRule.getVersion(), barcodeParseRuleDTO.getVersion()));

            barcodeRuleRepository.save(barcodeParseRuleTransfer.toDO(barcodeParseRuleDTO));
        } finally {
            distributeLock.releaseLock(BARCODE_PARSE_RULE_ADD_LOCK);
        }
    }

    @Override
    public BarcodeParseResult parse(BarcodeParseRequestDTO barcodeParseRequestDTO) {

        List<BarcodeParseRule> barcodeParseRules = queryParseRules(barcodeParseRequestDTO);
        if (CollectionUtils.isEmpty(barcodeParseRules)) {
            return convertToObject(barcodeParseRequestDTO);
        }

        for (BarcodeParseRule parseRule : barcodeParseRules) {
            Map<String, String> map = parseRule.parse(barcodeParseRequestDTO.getBarcode());
            if (MapUtils.isNotEmpty(map)) {
                return convertToObject(map);
            }
        }

        throw WmsException.throwWmsException(BARCODE_PARSE_ERROR, barcodeParseRequestDTO.getBarcode());
    }

    private BarcodeParseResult convertToObject(BarcodeParseRequestDTO barcodeParseRequestDTO) {

        BarcodeParseResult.BarcodeParseResultBuilder result = BarcodeParseResult.builder();
        if (barcodeParseRequestDTO.getExecuteTime() == ExecuteTimeEnum.SCAN_SKU) {
            List<SkuMainDataDTO> skuBarcodeDataDTOs = skuMainDataApi.querySkuBarcodeData(barcodeParseRequestDTO.getBarcode(), barcodeParseRequestDTO.getBarcode());
            result.skus(skuBarcodeDataDTOs);
        } else {
            result.containerCode(barcodeParseRequestDTO.getBarcode());
        }

        return result.build();
    }

    private BarcodeParseResult convertToObject(Map<String, String> map) {

        String skuCode = map.get(ParserObjectEnum.SKU_CODE.name());
        String barcode = map.get(ParserObjectEnum.BAR_CODE.name());

        BarcodeParseResult.BarcodeParseResultBuilder result = BarcodeParseResult.builder()
                .amount(map.getOrDefault(ParserObjectEnum.AMOUNT.name(), "1"))
                .containerCode(map.getOrDefault(ParserObjectEnum.CONTAINER_CODE.name(), ""))
                .containerFace(map.getOrDefault(ParserObjectEnum.CONTAINER_FACE.name(), ""))
                .attributes(map);

        if (skuCode != null || barcode != null) {
            List<SkuMainDataDTO> skuBarcodeDataDTOs = skuMainDataApi.querySkuBarcodeData(barcode, skuCode);
            result.skus(skuBarcodeDataDTOs);
        }

        return result.build();
    }

    private List<BarcodeParseRule> queryParseRules(BarcodeParseRequestDTO barcodeParseRequestDTO) {

        List<BarcodeParseRule> barcodeParseRules = barcodeRuleRepository
                .findAllByBusinessFlowAndExecuteTime(barcodeParseRequestDTO.getBusinessFlow(), barcodeParseRequestDTO.getExecuteTime())
                .stream().filter(BarcodeParseRule::isEnable).toList();

        //1. know sku
        if (CollectionUtils.isNotEmpty(barcodeParseRequestDTO.getKnownSkus())) {
            return barcodeParseRequestDTO.getKnownSkus().stream().flatMap(knownSku -> {
                SkuMainDataDTO skuMainDataDTO = skuMainDataApi.getSkuMainData(knownSku.getSkuCode(), knownSku.getOwnerCode());
                String barcodeRuleCode = skuMainDataDTO.getSkuConfig().getBarcodeRuleCode();
                if (StringUtils.isNotEmpty(barcodeRuleCode)) {
                    Optional<BarcodeParseRule> optional = barcodeParseRules.stream().filter(v -> StringUtils.equals(v.getCode(), barcodeRuleCode))
                            .findFirst();
                    if (optional.isPresent()) {
                        return Lists.newArrayList(optional.get()).stream();
                    }
                }
                return barcodeParseRules.stream()
                        .filter(barcodeParseRule -> barcodeParseRule.match(knownSku.getOwnerCode(), knownSku.getBrand()));
            }).toList();
        }

        //2. don't know sku,but know owner
        if (StringUtils.isNotEmpty(barcodeParseRequestDTO.getOwnerCode())) {
            return barcodeParseRules.stream()
                    .filter(barcodeParseRule -> barcodeParseRule.matchOwner(barcodeParseRequestDTO.getOwnerCode()))
                    .toList();
        }

        //3. don't know sku and owner
        return barcodeParseRules;
    }

    @Override
    public void enable(Long barcodeParseRuleId) {
        BarcodeParseRule barcodeParseRule = barcodeRuleRepository.findById(barcodeParseRuleId);
        barcodeParseRule.enable();
        barcodeRuleRepository.save(barcodeParseRule);
    }

    @Override
    public void disable(Long barcodeParseRuleId) {
        BarcodeParseRule barcodeParseRule = barcodeRuleRepository.findById(barcodeParseRuleId);
        barcodeParseRule.disable();
        barcodeRuleRepository.save(barcodeParseRule);
    }
}
