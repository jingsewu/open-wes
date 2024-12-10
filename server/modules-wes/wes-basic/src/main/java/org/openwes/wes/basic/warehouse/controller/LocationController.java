package org.openwes.wes.basic.warehouse.controller;

import com.google.common.collect.Lists;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.basic.IContainerSpecApi;
import org.openwes.wes.api.basic.ILocationApi;
import org.openwes.wes.api.basic.IWarehouseAreaApi;
import org.openwes.wes.basic.warehouse.controller.parameter.AisleLocationQueryVO;
import org.openwes.wes.basic.warehouse.controller.parameter.AisleLocationVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.openwes.wes.api.basic.dto.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.openwes.common.utils.exception.code_enum.BasicErrorDescEnum.FORBIDDEN_OPERATE_MULTIPLE_AISLE;

@RestController
@RequestMapping("basic/location")
@Validated
@RequiredArgsConstructor
@Tag(name = "Wms Module Api")
public class LocationController {

    private final ILocationApi locationApi;
    private final IWarehouseAreaApi warehouseAreaApi;
    private final IContainerSpecApi containerSpecApi;

    @PostMapping("create")
    public Object create(@RequestBody @Valid AisleLocationVO locationVO) {
        createLocations(locationVO);
        return Response.success();
    }

    @PostMapping("update")
    public Object update(@RequestBody @Valid LocationUpdateDTO locationUpdateDTO) {
        locationApi.update(locationUpdateDTO);
        return Response.success();
    }

    @PostMapping("delete/{id}")
    public Object delete(@PathVariable Long id) {
        locationApi.delete(id);
        return Response.success();
    }

    @PostMapping("getByAisle")
    public Object getByAisle(@RequestBody AisleLocationQueryVO aisleLocationQueryVO) {
        return Response.builder()
                .data(locationApi.getByAisle(aisleLocationQueryVO.getAisleCode(), aisleLocationQueryVO.getWarehouseAreaId()))
                .build();
    }

    @PostMapping("updateAisleLocation")
    public Object updateAisleLocation(@RequestBody @Valid AisleLocationVO aisleLocationVO) {
        Set<String> aisleCodes = aisleLocationVO.getLocationDescList().stream()
                .map(AisleLocationVO.LocationDesc::getAisleCode).collect(Collectors.toSet());
        if (aisleCodes.size() != 1) {
            throw WmsException.throwWmsException(FORBIDDEN_OPERATE_MULTIPLE_AISLE);
        }

        locationApi.deleteByAisle(aisleCodes.iterator().next(), aisleLocationVO.getWarehouseAreaId());
        createLocations(aisleLocationVO);

        return Response.success();
    }

    private void createLocations(@RequestBody @Valid AisleLocationVO aisleLocationVO) {
        WarehouseAreaDTO warehouseAreaDTO = warehouseAreaApi.getById(aisleLocationVO.getWarehouseAreaId());
        List<AisleDTO> aisleDTOS = Lists.newArrayList();
        List<LocationDTO> locationDTOS = Lists.newArrayList();
        buildLocationDTOS(aisleLocationVO, warehouseAreaDTO, aisleDTOS, locationDTOS);
        locationApi.createLocations(aisleDTOS, locationDTOS);
    }

    private void buildLocationDTOS(AisleLocationVO locationVO, WarehouseAreaDTO warehouseAreaDTO, List<AisleDTO> aisleDTOS, List<LocationDTO> locationDTOS) {
        locationVO.getLocationDescList()
                .stream().collect(Collectors.groupingBy(AisleLocationVO.LocationDesc::getAisleCode))
                .forEach((key, values) -> {
                    aisleDTOS.add(AisleDTO.builder()
                            .aisleCode(key)
                            .singleEntrance(values.get(0).isSingleEntrance())
                            .warehouseAreaId(locationVO.getWarehouseAreaId()).build());

                    locationDTOS.addAll(values.stream().flatMap(value -> {
                        value.setWarehouseAreaGroupCode(warehouseAreaDTO.getWarehouseGroupCode());
                        value.setWarehouseAreaCode(warehouseAreaDTO.getWarehouseAreaCode());
                        ContainerSpecDTO containerSpecDTO =
                                containerSpecApi.getContainerSpecDTO(value.getContainerSpecCode(), locationVO.getWarehouseCode());
                        return buildLocationDTO(containerSpecDTO, value, locationVO);
                    }).toList());

                });
    }

    private Stream<LocationDTO> buildLocationDTO(ContainerSpecDTO containerSpecDTO, AisleLocationVO.LocationDesc value,
                                                 AisleLocationVO locationVO) {
        return containerSpecDTO.getContainerSlotSpecs().stream()
                .flatMap(containerSlotSpec ->
                        IntStream.range(1, value.getShelfNumber() + 1)
                                .mapToObj(i ->
                                        LocationDTO.builder()
                                                .locationCode(value.generateLocationCode(i, containerSlotSpec.getLocBay(), containerSlotSpec.getLocLevel()))
                                                .locationType(locationVO.getLocationType())
                                                .aisleCode(value.getAisleCode())
                                                .occupied(false)
                                                .shelfCode(value.generateShelfCode(i))
                                                .warehouseCode(locationVO.getWarehouseCode())
                                                .warehouseLogicId(value.getWarehouseLogicId())
                                                .warehouseAreaId(locationVO.getWarehouseAreaId())
                                                .build()));
    }


}
