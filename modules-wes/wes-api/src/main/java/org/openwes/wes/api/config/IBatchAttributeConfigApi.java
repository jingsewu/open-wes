package org.openwes.wes.api.config;

import org.openwes.wes.api.config.dto.BatchAttributeConfigDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Collection;
import java.util.List;

public interface IBatchAttributeConfigApi {

    void save(@Valid BatchAttributeConfigDTO batchAttributeConfigDTO);

    void update(@Valid BatchAttributeConfigDTO batchAttributeConfigDTO);

    BatchAttributeConfigDTO getByOwnerAndSkuFirstCategory(@NotNull String ownerCode, @NotNull String skuFirstCategory);

    List<BatchAttributeConfigDTO> getByOwners(@NotNull Collection<String> ownerCode);
}
