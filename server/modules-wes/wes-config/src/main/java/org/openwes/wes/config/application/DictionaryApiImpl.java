package org.openwes.wes.config.application;

import org.openwes.wes.api.config.IDictionaryApi;
import org.openwes.wes.api.config.dto.DictionaryDTO;
import org.openwes.wes.config.domain.repository.DictionaryRepository;
import org.openwes.wes.config.domain.transfer.DictionaryTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import java.util.Map;
import java.util.stream.Collectors;
import org.openwes.common.utils.language.MultiLanguage;
import org.openwes.common.utils.language.core.LanguageContext;
import org.openwes.wes.config.domain.entity.Dictionary;

@Validated
@Service
@RequiredArgsConstructor
public class DictionaryApiImpl implements IDictionaryApi {

    private final DictionaryRepository dictionaryRepository;
    private final DictionaryTransfer dictionaryTransfer;

    @Override
    public void save(DictionaryDTO dictionaryDTO) {
        dictionaryRepository.save(dictionaryTransfer.toDO(dictionaryDTO));
    }

    @Override
    public void update(DictionaryDTO dictionaryDTO) {
        Dictionary existing = dictionaryRepository.findById(dictionaryDTO.getId());

        String lang = LanguageContext.getLanguage();
        Map<String, DictionaryDTO.DictionaryItem> dtoItemMap = dictionaryDTO.getItems().stream()
                .collect(Collectors.toMap(DictionaryDTO.DictionaryItem::getValue, i -> i));

        for (Dictionary.DictionaryItem existingItem : existing.getItems()) {
            DictionaryDTO.DictionaryItem dtoItem = dtoItemMap.get(existingItem.getValue());
            if (dtoItem == null) continue;
            if (existingItem.getCustomLabel() == null) {
                existingItem.setCustomLabel(new MultiLanguage(lang, dtoItem.getShowContent()));
            } else {
                existingItem.getCustomLabel().put(lang, dtoItem.getShowContent());
            }
        }

        dictionaryRepository.save(existing);
    }

    @Override
    public DictionaryDTO getByCode(String code) {
        return dictionaryTransfer.toDTO(dictionaryRepository.findByCode(code));
    }
}
