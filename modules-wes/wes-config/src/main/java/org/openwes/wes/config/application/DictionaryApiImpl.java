package org.openwes.wes.config.application;

import org.openwes.wes.api.config.IDictionaryApi;
import org.openwes.wes.api.config.dto.DictionaryDTO;
import org.openwes.wes.config.domain.repository.DictionaryRepository;
import org.openwes.wes.config.domain.transfer.DictionaryTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

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
        dictionaryRepository.save(dictionaryTransfer.toDO(dictionaryDTO));
    }

    @Override
    public DictionaryDTO getByCode(String code) {
        return dictionaryTransfer.toDTO(dictionaryRepository.findByCode(code));
    }
}
