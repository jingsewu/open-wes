package org.openwes.wes.basic.container.application;

import org.openwes.common.utils.validate.ValidationSequence;
import org.openwes.wes.api.basic.IContainerSpecApi;
import org.openwes.wes.api.basic.dto.ContainerSpecDTO;
import org.openwes.wes.basic.container.domain.entity.ContainerSpec;
import org.openwes.wes.basic.container.domain.repository.ContainerSpecRepository;
import org.openwes.wes.basic.container.domain.service.ContainerSpecService;
import org.openwes.wes.basic.container.domain.transfer.ContainerSpecTransfer;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

@Primary
@Service
@DubboService
@Validated(ValidationSequence.class)
@RequiredArgsConstructor
public class ContainerSpecApiImpl implements IContainerSpecApi {

    private final ContainerSpecRepository containerSpecRepository;
    private final ContainerSpecTransfer containerSpecTransfer;
    private final ContainerSpecService containerSpecService;

    @Override
    public void save(ContainerSpecDTO containerSpecDTO) {
        containerSpecRepository.save(containerSpecTransfer.toDO(containerSpecDTO));
    }

    @Override
    public void update(ContainerSpecDTO containerSpecDTO) {
        containerSpecRepository.save(containerSpecTransfer.toDO(containerSpecDTO));
    }

    @Override
    public ContainerSpecDTO getContainerSpecDTO(String containerSpecCode, String warehouseCode) {
        return containerSpecTransfer.toDTO(containerSpecRepository.findByContainerSpecCode(containerSpecCode, warehouseCode));
    }

    @Override
    public List<ContainerSpecDTO> queryContainerSpec(Collection<String> containerSpecCodes, String warehouseCode) {
        List<ContainerSpec> containerSpecs = containerSpecRepository.findAllByContainerSpecCodes(containerSpecCodes, warehouseCode);
        return containerSpecTransfer.toDTOs(containerSpecs);
    }

    @Override
    public void delete(Long id) {
        ContainerSpec containerSpec = containerSpecRepository.findById(id);
        containerSpecService.validateDelete(containerSpec);
        containerSpecRepository.delete(containerSpec);
    }

}
