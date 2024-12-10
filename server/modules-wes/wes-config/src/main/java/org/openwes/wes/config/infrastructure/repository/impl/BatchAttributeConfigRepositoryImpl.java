package org.openwes.wes.config.infrastructure.repository.impl;

import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.wes.config.domain.entity.BatchAttributeConfig;
import org.openwes.wes.config.domain.repository.BatchAttributeConfigRepository;
import org.openwes.wes.config.infrastructure.persistence.mapper.BatchAttributeConfigPORepository;
import org.openwes.wes.config.infrastructure.persistence.transfer.BatchAttributeConfigPOTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BatchAttributeConfigRepositoryImpl implements BatchAttributeConfigRepository {

    private final BatchAttributeConfigPORepository batchAttributeConfigPORepository;
    private final BatchAttributeConfigPOTransfer batchAttributeConfigPOTransfer;

    @Override
    public List<BatchAttributeConfig> findAll() {
        return batchAttributeConfigPOTransfer.toDOs(batchAttributeConfigPORepository.findAll());
    }

    @Override
    @CacheEvict(cacheNames = RedisConstants.BATCH_ATTRIBUTE_CONFIG_CACHE, key = "#batchAttributeConfig.code")
    public void save(BatchAttributeConfig batchAttributeConfig) {
        batchAttributeConfigPORepository.save(batchAttributeConfigPOTransfer.toPO(batchAttributeConfig));
    }

    @Override
    public BatchAttributeConfig findById(Long id) {
        return batchAttributeConfigPOTransfer.toDO(batchAttributeConfigPORepository.findById(id).orElseThrow());
    }

    @Override
    @Cacheable(cacheNames = RedisConstants.BATCH_ATTRIBUTE_CONFIG_CACHE, key = "#code")
    public BatchAttributeConfig findByCode(String code) {
        return batchAttributeConfigPOTransfer.toDO(batchAttributeConfigPORepository.findByCode(code));
    }
}
