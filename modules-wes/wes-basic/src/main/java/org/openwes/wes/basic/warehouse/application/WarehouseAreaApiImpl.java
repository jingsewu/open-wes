package org.openwes.wes.basic.warehouse.application;

import com.google.common.collect.Lists;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.BasicErrorDescEnum;
import org.openwes.wes.api.basic.IWarehouseAreaApi;
import org.openwes.wes.api.basic.constants.WarehouseAreaWorkTypeEnum;
import org.openwes.wes.api.basic.dto.WarehouseAreaDTO;
import org.openwes.wes.basic.warehouse.domain.entity.Location;
import org.openwes.wes.basic.warehouse.domain.entity.WarehouseArea;
import org.openwes.wes.basic.warehouse.domain.entity.WarehouseLogic;
import org.openwes.wes.basic.warehouse.domain.repository.LocationRepository;
import org.openwes.wes.basic.warehouse.domain.repository.WarehouseAreaRepository;
import org.openwes.wes.basic.warehouse.domain.repository.WarehouseLogicRepository;
import org.openwes.wes.basic.warehouse.domain.transfer.WarehouseAreaTransfer;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

@Service
@Validated
@DubboService
@RequiredArgsConstructor
public class WarehouseAreaApiImpl implements IWarehouseAreaApi {

    private final WarehouseAreaRepository warehouseAreaRepository;
    private final WarehouseLogicRepository warehouseLogicRepository;
    private final WarehouseAreaTransfer warehouseAreaTransfer;
    private final LocationRepository locationRepository;

    @Override
    public void save(WarehouseAreaDTO warehouseAreaDTO) {
        WarehouseArea warehouseArea = warehouseAreaRepository.findByCode(warehouseAreaDTO.getWarehouseAreaCode());
        if (warehouseArea != null) {
            throw WmsException.throwWmsException(BasicErrorDescEnum.WAREHOUSE_LOGIC_CODE_REPEATED, warehouseAreaDTO.getWarehouseAreaCode());
        }
        warehouseAreaRepository.save(warehouseAreaTransfer.toDO(warehouseAreaDTO));
    }

    @Override
    public void update(WarehouseAreaDTO warehouseAreaDTO) {
        warehouseAreaRepository.save(warehouseAreaTransfer.toDO(warehouseAreaDTO));
    }

    @Override
    @Transactional
    public void enable(Long id) {
        WarehouseArea warehouseArea = warehouseAreaRepository.findById(id);
        List<Location> locations = locationRepository.findAllByWarehouseAreaId(warehouseArea.getId());

        locations.forEach(Location::enable);
        locationRepository.saveAll(locations);

        warehouseArea.enable();
        warehouseAreaRepository.save(warehouseArea);
    }

    @Override
    @Transactional
    public void disable(Long id) {
        WarehouseArea warehouseArea = warehouseAreaRepository.findById(id);
        List<Location> locations = locationRepository.findAllByWarehouseAreaId(warehouseArea.getId());

        locations.forEach(Location::disable);
        locationRepository.saveAll(locations);

        warehouseArea.disable();
        warehouseAreaRepository.save(warehouseArea);
    }

    @Override
    public void delete(Long id) {

        WarehouseArea warehouseArea = warehouseAreaRepository.findById(id);
        List<WarehouseLogic> warehouseLogics = warehouseLogicRepository.findAllByWarehouseAreaIds(Lists.newArrayList(warehouseArea.getId()));

        warehouseLogics.forEach(WarehouseLogic::delete);
        warehouseLogicRepository.saveAll(warehouseLogics);

        warehouseArea.delete();
        warehouseAreaRepository.save(warehouseArea);
    }

    @Override
    public WarehouseAreaDTO getById(Long warehouseAreaId) {
        return warehouseAreaTransfer.toDTO(warehouseAreaRepository.findById(warehouseAreaId));
    }

    @Override
    public List<WarehouseAreaDTO> getByIds(Collection<Long> warehouseAreaIds) {
        return warehouseAreaTransfer.toDTOs(warehouseAreaRepository.findAllByIds(warehouseAreaIds));
    }

    @Override
    public List<WarehouseAreaDTO> getByWarehouseCode(String warehouseCode) {
        return warehouseAreaTransfer.toDTOs(warehouseAreaRepository.findAllByWarehouseCode(warehouseCode));
    }

    @Override
    public Collection<WarehouseAreaDTO> findByWarehouseAreaWorkType(WarehouseAreaWorkTypeEnum warehouseAreaWorkTypeEnum) {
        return warehouseAreaTransfer.toDTOs(warehouseAreaRepository.findAllByWarehouseAreaWorkType(warehouseAreaWorkTypeEnum));
    }

}
