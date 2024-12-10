package org.openwes.wes.config.domain.repository;

import org.openwes.wes.config.domain.entity.BatchAttributeConfig;

import java.util.List;

public interface BatchAttributeConfigRepository {

    void save(BatchAttributeConfig toBatchAttributeConfig);

    List<BatchAttributeConfig> findAll();

    BatchAttributeConfig findById(Long id);

    BatchAttributeConfig findByCode(String code);
}
