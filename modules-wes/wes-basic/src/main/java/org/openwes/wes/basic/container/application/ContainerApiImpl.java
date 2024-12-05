package org.openwes.wes.basic.container.application;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.openwes.wes.api.basic.IContainerApi;
import org.openwes.wes.basic.container.domain.entity.Container;
import org.openwes.wes.basic.container.domain.entity.ContainerSpec;
import org.openwes.wes.basic.container.domain.repository.ContainerRepository;
import org.openwes.wes.basic.container.domain.repository.ContainerSpecRepository;
import org.openwes.wes.basic.container.domain.transfer.ContainerSpecTransfer;
import org.openwes.wes.basic.container.domain.transfer.ContainerTransfer;
import org.openwes.wes.basic.container.domain.aggregate.ContainerLocationAggregate;
import org.openwes.wes.basic.warehouse.domain.entity.Location;
import org.openwes.wes.basic.warehouse.domain.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.openwes.wes.api.basic.dto.*;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.openwes.common.utils.exception.code_enum.BasicErrorDescEnum.CONTAINER_SPECIFIC_NOT_EXIST;

@Primary
@Service
@Validated
@DubboService
@RequiredArgsConstructor
public class ContainerApiImpl implements IContainerApi {

    private final ContainerRepository containerRepository;
    private final ContainerSpecRepository containerSpecRepository;
    private final ContainerTransfer containerTransfer;
    private final ContainerSpecTransfer containerSpecTransfer;
    private final ContainerLocationAggregate containerLocationAggregate;
    private final LocationRepository locationRepository;

    @Override
    public void createContainer(String warehouseCode, String containerCode, String containerSpecCode) {
        ContainerSpec containerSpec = containerSpecRepository.findByContainerSpecCode(containerSpecCode, warehouseCode);
        Preconditions.checkState(containerSpec != null, CONTAINER_SPECIFIC_NOT_EXIST.getDesc());

        Container container = new Container(warehouseCode, containerCode, containerSpecCode,
                containerSpecTransfer.toContainerSlots(containerSpec.getContainerSlotSpecs()));
        containerRepository.save(container);
    }

    @Override
    public void createContainer(String warehouseCode, List<CreateEmptyContainerDTO> containerDTOS) {
        Set<String> containerSpecCodes = containerDTOS.stream()
                .map(CreateEmptyContainerDTO::getContainerCodeSpec).collect(Collectors.toSet());
        List<ContainerSpec> containerSpecs = containerSpecRepository.findAllByContainerSpecCodes(containerSpecCodes, warehouseCode);

        Map<String, ContainerSpec> containerSpecMap = containerSpecs.stream()
                .collect(Collectors.toMap(ContainerSpec::getContainerSpecCode, v -> v));

        List<Container> containers = containerDTOS.stream().map(dto -> {
            ContainerSpec containerSpec = containerSpecMap.get(dto.getContainerCodeSpec());
            return new Container(warehouseCode, dto.getContainerCode(), dto.getContainerCodeSpec(),
                    containerSpecTransfer.toContainerSlots(containerSpec.getContainerSlotSpecs()));
        }).toList();

        containerRepository.saveAll(containers);
    }

    @Override
    public void batchCreateContainer(BatchCreateContainerDTO createContainerDTO) {
        ContainerSpec containerSpec = containerSpecRepository
                .findByContainerSpecCode(createContainerDTO.getContainerSpecCode(), createContainerDTO.getWarehouseCode());
        Preconditions.checkState(containerSpec != null, CONTAINER_SPECIFIC_NOT_EXIST.getDesc());

        String format = "%0" + createContainerDTO.getIndexNumber() + "d";

        List<Container> containers = Lists.newArrayList();
        int endIndex = createContainerDTO.getStartIndex() + createContainerDTO.getCreateNumber();
        for (int i = createContainerDTO.getStartIndex(); i <= endIndex; i++) {
            String containerCode = createContainerDTO.getContainerCodePrefix() + String.format(format, i);
            containers.add(new Container(createContainerDTO.getWarehouseCode(), containerCode,
                    createContainerDTO.getContainerSpecCode(),
                    containerSpecTransfer.toContainerSlots(containerSpec.getContainerSlotSpecs())));
        }

        containerRepository.saveAll(containers);
    }

    @Override
    public ContainerSpecDTO queryContainerLayout(String containerCode, String warehouseCode, String face) {
        Container container = containerRepository.findByContainerCode(containerCode, warehouseCode);
        ContainerSpec containerSpec = containerSpecRepository
                .findByContainerSpecCode(container.getContainerSpecCode(), container.getWarehouseCode());
        List<ContainerSpecDTO.ContainerSlotSpec> containerSlotSpecs = containerSpec.getContainerSlotSpecsByFace(face);
        containerSpec.setContainerSlotSpecs(containerSlotSpecs);
        return containerSpecTransfer.toDTO(containerSpec);
    }

    @Override
    public void changeContainerSpec(String warehouseCode, String containerCode, String containerSpecCode) {
        Container container = containerRepository.findByContainerCode(containerCode, warehouseCode);
        changeContainerSpec(containerSpecCode, container);
    }

    @Override
    public void changeContainerSpec(Long containerId, String containerSpecCode) {
        Container container = containerRepository.findById(containerId);
        changeContainerSpec(containerSpecCode, container);
    }

    private void changeContainerSpec(String containerSpecCode, Container container) {
        if (StringUtils.equals(container.getContainerSpecCode(), containerSpecCode)) {
            return;
        }
        ContainerSpec containerSpec = containerSpecRepository
                .findByContainerSpecCode(containerSpecCode, container.getWarehouseCode());
        Preconditions.checkState(containerSpec != null, CONTAINER_SPECIFIC_NOT_EXIST.getDesc());

        container.changeContainerSpec(containerSpecCode,
                containerSpecTransfer.toContainerSlots(containerSpec.getContainerSlotSpecs()));
        containerRepository.save(container);
    }

    @Override
    public ContainerDTO queryContainer(String containerCode, String warehouseCode) {
        Container container = containerRepository.findByContainerCode(containerCode, warehouseCode);
        return containerTransfer.toDTO(container);
    }

    @Override
    public Collection<ContainerDTO> queryContainer(Collection<String> containerCodes, String warehouseCode) {
        List<Container> containers = containerRepository.findByContainerCodes(containerCodes, warehouseCode);
        return containerTransfer.toDTOs(containers);
    }

    @Override
    public void lockContainer(String warehouseCode, Set<String> containerCodes) {
        final List<Container> containers = containerRepository.findByContainerCodes(containerCodes, warehouseCode);
        containers.forEach(Container::lock);
        containerRepository.saveAll(containers);
    }

    @Override
    public void unLockContainer(String warehouseCode, Set<String> containerCodes) {
        final List<Container> containers = containerRepository.findByContainerCodes(containerCodes, warehouseCode);
        containers.forEach(Container::unlock);
        containerRepository.saveAll(containers);
    }

    @Override
    public void updateContainerLocation(List<ContainerLocationReportDTO> reportDTOS) {
        reportDTOS.stream().collect(Collectors.groupingBy(ContainerLocationReportDTO::getWarehouseAreaId))
                .forEach((warehouseAreaId, values) -> {

                    Set<String> containerCodes = values.stream()
                            .map(ContainerLocationReportDTO::getContainerCode)
                            .collect(Collectors.toSet());

                    String warehouseCode = values.iterator().next().getWarehouseCode();

                    List<Container> containers = containerRepository.findByContainerCodes(containerCodes, warehouseCode);
                    List<String> locationCodes = values.stream().map(ContainerLocationReportDTO::getLocationCode).toList();
                    List<Location> locations = locationRepository.findAllByLocationCodes(locationCodes, warehouseAreaId);
                    containerLocationAggregate.updateContainerLocation(values, containers, locations);

                });
    }
}
