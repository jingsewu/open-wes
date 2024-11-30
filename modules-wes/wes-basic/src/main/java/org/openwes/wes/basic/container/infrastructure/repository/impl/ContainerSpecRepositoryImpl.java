package org.openwes.wes.basic.container.infrastructure.repository.impl;

import org.openwes.wes.basic.container.domain.entity.ContainerSpec;
import org.openwes.wes.basic.container.domain.repository.ContainerSpecRepository;
import org.openwes.wes.basic.container.infrastructure.persistence.mapper.ContainerSpecPORepository;
import org.openwes.wes.basic.container.infrastructure.persistence.po.ContainerSpecPO;
import org.openwes.wes.basic.container.infrastructure.persistence.transfer.ContainerSpecPOTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContainerSpecRepositoryImpl implements ContainerSpecRepository {

    private final ContainerSpecPORepository containerSpecPORepository;
    private final ContainerSpecPOTransfer containerSpecPOTransfer;

    @Override
    public ContainerSpec findByContainerSpecCode(String containerSpecCode, String warehouseCode) {
        return containerSpecPOTransfer.toDO(containerSpecPORepository
                .findByContainerSpecCodeAndWarehouseCode(containerSpecCode, warehouseCode));
    }

    @Override
    public void save(ContainerSpec containerSpec) {
        containerSpecPORepository.save(containerSpecPOTransfer.toPO(containerSpec));
    }

    @Override
    public ContainerSpec findById(Long id) {
        return containerSpecPOTransfer.toDO(containerSpecPORepository.findById(id).orElseThrow());
    }

    @Override
    public List<ContainerSpec> findAllByContainerSpecCodes(Collection<String> containerSpecCodes, String warehouseCode) {
        List<ContainerSpecPO> containerSpecPOS = containerSpecPORepository.findByContainerSpecCodeInAndWarehouseCode(containerSpecCodes, warehouseCode);
        return containerSpecPOTransfer.toDOs(containerSpecPOS);
    }

    @Override
    public List<ContainerSpec> findAll() {
        List<ContainerSpecPO> specPOS = containerSpecPORepository.findAll();
        return containerSpecPOTransfer.toDOs(specPOS);
    }

    @Override
    public List<ContainerSpec> findAllByContainerSpecCodeAndWarehouseCode(Collection<String> containerSpecCodes, Collection<String> warehouseCodes) {
        return containerSpecPOTransfer.toDOs(containerSpecPORepository.findAllByContainerSpecCodeInAndWarehouseCodeIn(containerSpecCodes, warehouseCodes));
    }

    @Override
    public void delete(ContainerSpec containerSpec) {
        containerSpecPORepository.delete(containerSpecPOTransfer.toPO(containerSpec));
    }

}
