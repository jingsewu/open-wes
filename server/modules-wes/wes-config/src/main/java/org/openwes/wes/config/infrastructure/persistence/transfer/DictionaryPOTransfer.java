package org.openwes.wes.config.infrastructure.persistence.transfer;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

import org.openwes.wes.config.domain.entity.Dictionary;
import org.openwes.wes.config.infrastructure.persistence.po.DictionaryPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DictionaryPOTransfer {
    DictionaryPO toPO(Dictionary dictionary);

    List<DictionaryPO> toPOs(List<Dictionary> dictionary);

    Dictionary toDO(DictionaryPO id);

    List<Dictionary> toDOs(List<DictionaryPO> dictionaryPOS);
}
