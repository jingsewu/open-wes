package org.openwes.wes.config.domain.transfer;

import org.openwes.common.utils.language.MultiLanguage;
import org.openwes.common.utils.language.core.LanguageContext;
import org.openwes.wes.api.config.dto.DictionaryDTO;
import org.openwes.wes.config.domain.entity.Dictionary;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DictionaryTransfer {

    @Mapping(source = "name", target = "name", qualifiedByName = "toMultiLanguage")
    @Mapping(source = "description", target = "description", qualifiedByName = "toMultiLanguage")
    @Mapping(source = "items", target = "items", qualifiedByName = "toDOItem")
    Dictionary toDO(DictionaryDTO dictionaryDTO);

    default List<Dictionary> toDOs(List<DictionaryDTO> dictionaryDTOS) {
        return dictionaryDTOS.stream().map(this::toDO).collect(Collectors.toList());
    }

    @Mapping(source = "name", target = "name", qualifiedByName = "toCurrentLanguage")
    @Mapping(source = "description", target = "description", qualifiedByName = "toCurrentLanguage")
    @Mapping(source = "items", target = "items", qualifiedByName = "toDTOItem")
    DictionaryDTO toDTO(Dictionary dictionary);

    /**
     * Admin UI write path: showContent → customLabel.
     * systemLabel is never written by this path — it is only written by toSystemLabelDOItem (refresh/Liquibase).
     */
    @Named("toDOItem")
    default Dictionary.DictionaryItem toDOItem(DictionaryDTO.DictionaryItem dto) {
        Dictionary.DictionaryItem item = new Dictionary.DictionaryItem();
        item.setValue(dto.getValue());
        item.setOrder(dto.getOrder());
        item.setDefaultItem(dto.isDefaultItem());
        item.setDescription(new MultiLanguage(LanguageContext.getLanguage(), dto.getDescription()));
        item.setCustomLabel(new MultiLanguage(LanguageContext.getLanguage(), dto.getShowContent()));
        return item;
    }

    /**
     * refresh() write path: showContent → systemLabel only, do not touch customLabel.
     */
    @Named("toSystemLabelDOItem")
    default Dictionary.DictionaryItem toSystemLabelDOItem(DictionaryDTO.DictionaryItem dto) {
        Dictionary.DictionaryItem item = new Dictionary.DictionaryItem();
        item.setValue(dto.getValue());
        item.setOrder(dto.getOrder());
        item.setDefaultItem(dto.isDefaultItem());
        item.setDescription(new MultiLanguage(LanguageContext.getLanguage(), dto.getDescription()));
        item.setSystemLabel(new MultiLanguage(LanguageContext.getLanguage(), dto.getShowContent()));
        return item;
    }

    /**
     * refresh() batch entry point.
     */
    default Dictionary toSystemLabelDO(DictionaryDTO dto) {
        Dictionary dictionary = new Dictionary();
        dictionary.setCode(dto.getCode());
        dictionary.setEditable(dto.isEditable());
        dictionary.setName(toMultiLanguage(dto.getName()));
        dictionary.setDescription(toMultiLanguage(dto.getDescription()));
        dictionary.setItems(dto.getItems().stream()
                .map(this::toSystemLabelDOItem)
                .collect(Collectors.toList()));
        return dictionary;
    }

    default List<Dictionary> toSystemLabelDOs(List<DictionaryDTO> dtos) {
        return dtos.stream().map(this::toSystemLabelDO).collect(Collectors.toList());
    }

    /**
     * Read path: three-level fallback → showContent; systemLabel → systemContent (admin reference).
     */
    @Named("toDTOItem")
    default DictionaryDTO.DictionaryItem toDTOItem(Dictionary.DictionaryItem item) {
        DictionaryDTO.DictionaryItem dto = new DictionaryDTO.DictionaryItem();
        dto.setValue(item.getValue());
        dto.setOrder(item.getOrder());
        dto.setDefaultItem(item.isDefaultItem());
        dto.setDescription(toCurrentLanguage(item.getDescription()));
        dto.setShowContent(resolveLabel(item));
        dto.setSystemContent(item.getSystemLabel() != null ? toCurrentLanguage(item.getSystemLabel()) : "");
        return dto;
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

    /**
     * Three-level fallback: customLabel[lang] → systemLabel[lang] → systemLabel["zh-CN"] → ""
     */
    static String resolveLabel(Dictionary.DictionaryItem item) {
        String lang = LanguageContext.getLanguage();
        if (item.getCustomLabel() != null) {
            String v = item.getCustomLabel().get(lang);
            if (v != null && !v.isEmpty()) return v;
        }
        if (item.getSystemLabel() != null) {
            String v = item.getSystemLabel().get(lang);
            if (v != null && !v.isEmpty()) return v;
            String zh = item.getSystemLabel().get("zh-CN");
            if (zh != null && !zh.isEmpty()) return zh;
        }
        return "";
    }
}
