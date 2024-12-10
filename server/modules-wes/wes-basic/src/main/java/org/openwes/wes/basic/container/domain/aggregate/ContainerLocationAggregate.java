package org.openwes.wes.basic.container.domain.aggregate;

import org.openwes.wes.api.basic.dto.ContainerLocationReportDTO;
import org.openwes.wes.basic.container.domain.entity.Container;
import org.openwes.wes.basic.container.domain.repository.ContainerRepository;
import org.openwes.wes.basic.warehouse.domain.entity.Location;
import org.openwes.wes.basic.warehouse.domain.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Validated
@RequiredArgsConstructor
public class ContainerLocationAggregate {

    private final ContainerRepository containerRepository;
    private final LocationRepository locationRepository;

    @Transactional(rollbackFor = Exception.class)
    public void updateContainerLocation(List<ContainerLocationReportDTO> reportDTOS, List<Container> containers, List<Location> locations) {

        Map<String, Container> containerMap = containers.stream().collect(Collectors.toMap(Container::getContainerCode, v -> v));
        Map<String, Location> locationMap = locations.stream().collect(Collectors.toMap(Location::getLocationCode, v -> v));

        reportDTOS.forEach(reportDTO -> {
            Container container = containerMap.get(reportDTO.getContainerCode());
            if (container != null) {
                container.changeLocation(reportDTO.getWarehouseCode(), reportDTO.getWarehouseAreaId(),
                        reportDTO.getLocationCode(), "");
            }
            Location location = locationMap.get(reportDTO.getLocationCode());
            if (location != null) {
                location.changeContainer(reportDTO.getContainerCode());
            }
        });

        containerRepository.saveAll(containers);
        locationRepository.saveAll(locations);
    }
}
