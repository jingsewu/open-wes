package org.openwes.wes.api.config;

import org.openwes.wes.api.config.dto.DictionaryDTO;
import jakarta.validation.Valid;

public interface IDictionaryApi {

    void save(@Valid DictionaryDTO dictionaryDTO);

    void update(@Valid DictionaryDTO dictionaryDTO);

    DictionaryDTO getByCode(String code);
}
