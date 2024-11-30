package org.openwes.wes.config.application;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.distribute.lock.DistributeLock;
import org.openwes.wes.api.config.IBatchAttributeConfigApi;
import org.openwes.wes.api.config.dto.BatchAttributeConfigDTO;
import org.openwes.wes.config.domain.entity.BatchAttributeConfig;
import org.openwes.wes.config.domain.repository.BatchAttributeConfigRepository;
import org.openwes.wes.config.domain.transfer.BatchAttributeConfigTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static org.openwes.common.utils.constants.RedisConstants.BATCH_ATTRIBUTE_CONFIG_ADD_LOCK;
import static org.openwes.common.utils.exception.code_enum.BasicErrorDescEnum.BARCODE_PARSE_RULE_REPEAT;

@Service
@Validated
@RequiredArgsConstructor
public class BatchAttributeConfigApiImpl implements IBatchAttributeConfigApi {

    private final DistributeLock distributeLock;
    private final BatchAttributeConfigRepository batchAttributeConfigRepository;
    private final BatchAttributeConfigTransfer batchAttributeConfigTransfer;

    @Override
    public void save(BatchAttributeConfigDTO batchAttributeConfigDTO) {
        distributeLock.acquireLockIfThrows(BATCH_ATTRIBUTE_CONFIG_ADD_LOCK, 1000);
        try {
            List<BatchAttributeConfig> batchAttributeConfigs = batchAttributeConfigRepository
                    .findAll().stream().filter(BatchAttributeConfig::isEnable).toList();
            if (batchAttributeConfigs.stream().anyMatch(batchAttributeConfig ->
                    batchAttributeConfig.match(batchAttributeConfigDTO.getOwnerCode(), batchAttributeConfigDTO.getSkuFirstCategory()))) {
                throw new WmsException(BARCODE_PARSE_RULE_REPEAT);
            }
            batchAttributeConfigRepository.save(batchAttributeConfigTransfer.toDO(batchAttributeConfigDTO));
        } finally {
            distributeLock.releaseLock(BATCH_ATTRIBUTE_CONFIG_ADD_LOCK);
        }
    }

    @Override
    public void update(BatchAttributeConfigDTO batchAttributeConfigDTO) {
        batchAttributeConfigRepository.save(batchAttributeConfigTransfer.toDO(batchAttributeConfigDTO));
    }

    @Override
    public BatchAttributeConfigDTO getByOwnerAndSkuFirstCategory(String ownerCode, String skuFirstCategory) {
        List<BatchAttributeConfig> batchAttributeConfigs = batchAttributeConfigRepository
                .findAll().stream().filter(BatchAttributeConfig::isEnable).toList();
        return batchAttributeConfigs.stream()
                .filter(batchAttributeConfig -> batchAttributeConfig.match(ownerCode, skuFirstCategory))
                .findFirst()
                .map(batchAttributeConfigTransfer::toDTO)
                .orElse(null);
    }

    @Override
    public List<BatchAttributeConfigDTO> getByOwners(Collection<String> ownerCodes) {
        List<BatchAttributeConfig> batchAttributeConfigs = batchAttributeConfigRepository
                .findAll().stream().filter(BatchAttributeConfig::isEnable).toList();
        return batchAttributeConfigs.stream()
                .filter(batchAttributeConfig -> ownerCodes.stream().anyMatch(batchAttributeConfig::matchOwner))
                .map(batchAttributeConfigTransfer::toDTO)
                .toList();
    }
}
