package org.openwes.wes.config.domain.transfer;

import org.openwes.common.utils.language.MultiLanguage;
import org.openwes.common.utils.language.core.LanguageContext;
import org.openwes.wes.api.config.dto.DictionaryDTO;
import org.openwes.wes.config.domain.entity.Dictionary;
import org.mapstruct.*;

import java.util.List;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DictionaryTransfer {

    @Mapping(source = "items", target = "items")
    @Mapping(source = "name", target = "name", qualifiedByName = "toMultiLanguage")
    @Mapping(source = "description", target = "description", qualifiedByName = "toMultiLanguage")
    Dictionary toDO(DictionaryDTO dictionaryDTO);

    List<Dictionary> toDOs(List<DictionaryDTO> dictionaryDTOS);

    @Mapping(source = "name", target = "name", qualifiedByName = "toCurrentLanguage")
    @Mapping(source = "description", target = "description", qualifiedByName = "toCurrentLanguage")
    @Mapping(source = "items", target = "items")
    DictionaryDTO toDTO(Dictionary dictionary);

    default Dictionary.DictionaryItem toDOItem(DictionaryDTO.DictionaryItem dictionaryItemDTO) {
        Dictionary.DictionaryItem dictionaryItem = new Dictionary.DictionaryItem();
        dictionaryItem.setDefaultItem(dictionaryItemDTO.isDefaultItem());
        dictionaryItem.setDescription(new MultiLanguage(LanguageContext.getLanguage(), dictionaryItemDTO.getDescription()));
        dictionaryItem.setOrder(dictionaryItemDTO.getOrder());
        dictionaryItem.setShowContext(new MultiLanguage(LanguageContext.getLanguage(), dictionaryItemDTO.getShowContent()));
        dictionaryItem.setValue(dictionaryItemDTO.getValue());
        return dictionaryItem;
    }

    default DictionaryDTO.DictionaryItem toDTOItem(Dictionary.DictionaryItem dictionaryItem) {
        DictionaryDTO.DictionaryItem dictionaryItemDTO = new DictionaryDTO.DictionaryItem();
        dictionaryItemDTO.setDefaultItem(dictionaryItem.isDefaultItem());
        dictionaryItemDTO.setDescription(toCurrentLanguage(dictionaryItem.getDescription()));
        dictionaryItemDTO.setOrder(dictionaryItem.getOrder());
        dictionaryItemDTO.setShowContent(toCurrentLanguage(dictionaryItem.getShowContext()));
        dictionaryItemDTO.setValue(dictionaryItem.getValue());
        return dictionaryItemDTO;
    }

    @Named("toMultiLanguage")
    static MultiLanguage toMultiLanguage(String value) {
        return new MultiLanguage(LanguageContext.getLanguage(), value);
    }

    @Named("toCurrentLanguage")
    static String toCurrentLanguage(MultiLanguage language) {
        if (language == null) {
            return "";
        }
        return language.get();
    }
}
