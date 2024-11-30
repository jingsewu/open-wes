package org.openwes.wes.basic.main.data.application;

import org.openwes.wes.api.main.data.IOwnerMainDataApi;
import org.openwes.wes.api.main.data.dto.OwnerMainDataDTO;
import org.openwes.wes.basic.main.data.domain.repository.OwnerMainDataRepository;
import org.openwes.wes.basic.main.data.domain.transfer.OwnerMainDataTransfer;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Primary
@Validated
@DubboService
@RequiredArgsConstructor
public class OwnerMainDataApiImpl implements IOwnerMainDataApi {

    private final OwnerMainDataRepository ownerMainDataRepository;
    private final OwnerMainDataTransfer ownerMainDataTransfer;

    @Override
    public void createOwner(OwnerMainDataDTO ownerMainDataDTO) {
        ownerMainDataRepository.save(ownerMainDataTransfer.toDO(ownerMainDataDTO));
    }

    @Override
    public void updateOwner(OwnerMainDataDTO ownerMainDataDTO) {
        ownerMainDataRepository.save(ownerMainDataTransfer.toDO(ownerMainDataDTO));
    }

    @Override
    public Collection<OwnerMainDataDTO> getOwners(Collection<String> ownerCodes, String warehouseCode) {
        return ownerMainDataRepository.findAllByOwnerCodes(ownerCodes, warehouseCode)
                .stream()
                .map(ownerMainDataTransfer::toDTO)
                .collect(Collectors.toSet());
    }
}
