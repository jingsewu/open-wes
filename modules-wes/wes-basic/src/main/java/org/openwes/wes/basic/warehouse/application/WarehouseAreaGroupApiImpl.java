package org.openwes.wes.basic.warehouse.application;

import org.openwes.wes.api.basic.IWarehouseAreaGroupApi;
import org.openwes.wes.api.basic.dto.WarehouseAreaGroupDTO;
import org.openwes.wes.basic.warehouse.domain.entity.Location;
import org.openwes.wes.basic.warehouse.domain.entity.WarehouseArea;
import org.openwes.wes.basic.warehouse.domain.entity.WarehouseAreaGroup;
import org.openwes.wes.basic.warehouse.domain.entity.WarehouseLogic;
import org.openwes.wes.basic.warehouse.domain.repository.LocationRepository;
import org.openwes.wes.basic.warehouse.domain.repository.WarehouseAreaGroupRepository;
import org.openwes.wes.basic.warehouse.domain.repository.WarehouseAreaRepository;
import org.openwes.wes.basic.warehouse.domain.repository.WarehouseLogicRepository;
import org.openwes.wes.basic.warehouse.domain.service.WarehouseService;
import org.openwes.wes.basic.warehouse.domain.transfer.WarehouseAreaGroupTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
public class WarehouseAreaGroupApiImpl implements IWarehouseAreaGroupApi {

    private final WarehouseAreaGroupRepository warehouseAreaGroupRepository;
    private final WarehouseLogicRepository warehouseLogicRepository;
    private final WarehouseAreaRepository warehouseAreaRepository;
    private final WarehouseAreaGroupTransfer warehouseAreaGroupTransfer;
    private final WarehouseService warehouseAreaGroupService;
    private final LocationRepository locationRepository;

    @Override
    public void save(WarehouseAreaGroupDTO warehouseAreaGroupDTO) {
        warehouseAreaGroupRepository.save(warehouseAreaGroupTransfer.toDO(warehouseAreaGroupDTO));
    }

    @Override
    public void update(WarehouseAreaGroupDTO warehouseAreaGroupDTO) {
        warehouseAreaGroupRepository.save(warehouseAreaGroupTransfer.toDO(warehouseAreaGroupDTO));
    }

    @Override
    @Transactional
    public void enable(Long id) {
        WarehouseAreaGroup warehouseAreaGroup = warehouseAreaGroupRepository.findById(id);

        List<Location> locations = warehouseAreaGroupService.getLocationsByWarehouseAreaGroup(warehouseAreaGroup);
        locations.forEach(Location::enable);
        locationRepository.saveAll(locations);

        warehouseAreaGroup.enable();
        warehouseAreaGroupRepository.save(warehouseAreaGroup);
    }

    @Override
    @Transactional
    public void disable(Long id) {
        WarehouseAreaGroup warehouseAreaGroup = warehouseAreaGroupRepository.findById(id);

        List<Location> locations = warehouseAreaGroupService.getLocationsByWarehouseAreaGroup(warehouseAreaGroup);
        locations.forEach(Location::disable);
        locationRepository.saveAll(locations);

        warehouseAreaGroup.disable();
        warehouseAreaGroupRepository.save(warehouseAreaGroup);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        WarehouseAreaGroup warehouseAreaGroup = warehouseAreaGroupRepository.findById(id);

        List<WarehouseArea> warehouseAreas = warehouseAreaRepository
                .findAllByWarehouseAreaGroup(warehouseAreaGroup.getWarehouseAreaGroupCode(), warehouseAreaGroup.getWarehouseCode());
        List<WarehouseLogic> warehouseLogics = warehouseLogicRepository
                .findAllByWarehouseAreaIds(warehouseAreas.stream().map(WarehouseArea::getId).toList());

        warehouseLogics.forEach(WarehouseLogic::delete);
        warehouseLogicRepository.saveAll(warehouseLogics);

        warehouseAreas.forEach(WarehouseArea::delete);
        warehouseAreaRepository.saveAll(warehouseAreas);

        warehouseAreaGroup.delete();
        warehouseAreaGroupRepository.save(warehouseAreaGroup);
    }
}
