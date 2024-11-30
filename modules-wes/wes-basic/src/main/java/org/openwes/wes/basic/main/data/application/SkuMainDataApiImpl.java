package org.openwes.wes.basic.main.data.application;

import org.openwes.wes.api.main.data.ISkuMainDataApi;
import org.openwes.wes.api.main.data.dto.SkuBarcodeDataDTO;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.basic.main.data.domain.aggregate.SkuMainDataAggregate;
import org.openwes.wes.basic.main.data.domain.entity.SkuBarcodeData;
import org.openwes.wes.basic.main.data.domain.entity.SkuMainData;
import org.openwes.wes.basic.main.data.domain.repository.SkuMainDataRepository;
import org.openwes.wes.basic.main.data.domain.service.SkuMainDataService;
import org.openwes.wes.basic.main.data.domain.transfer.SkuBarcodeDataTransfer;
import org.openwes.wes.basic.main.data.domain.transfer.SkuMainDataTransfer;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Validated
@Service
@Primary
@DubboService
@RequiredArgsConstructor
public class SkuMainDataApiImpl implements ISkuMainDataApi {

    private final SkuMainDataService skuMainDataService;
    private final SkuMainDataRepository skuMainDataRepository;
    private final SkuMainDataTransfer skuMainDataTransfer;
    private final SkuBarcodeDataTransfer skuBarcodeDataTransfer;
    private final SkuMainDataAggregate skuMainDataAggregate;

    @Override
    public void createOrUpdateBatch(List<SkuMainDataDTO> skuMainDataDTOs) {

        Set<String> skuCodes = skuMainDataDTOs.stream().map(SkuMainDataDTO::getSkuCode).collect(Collectors.toSet());
        List<SkuMainData> skuMainDataList = skuMainDataRepository.findAllBySkuCodes(skuCodes);

        List<SkuMainData> changedSkuMainDataList = skuMainDataDTOs.stream().map(skuDto -> {

            SkuMainData skuMainData = skuMainDataList.stream().filter(v -> v.equalsDto(skuDto)).findFirst().orElse(null);
            if (skuMainData == null) {
                skuMainData = skuMainDataTransfer.toDO(skuDto);
            } else {
                skuMainDataTransfer.toDo(skuMainData, skuDto);
            }
            return skuMainData;
        }).toList();

        skuMainDataService.validate(changedSkuMainDataList);

        List<Long> updatedSkuMainDataIds = changedSkuMainDataList.stream().map(SkuMainData::getId)
                .filter(id -> id != null && id > 0).toList();
        List<SkuBarcodeData> skuBarcodeData = skuMainDataRepository.findBarcodeBySkuIds(updatedSkuMainDataIds);
        skuMainDataAggregate.create(changedSkuMainDataList, skuBarcodeData);
    }

    @Override
    public List<SkuBarcodeDataDTO> querySkuBarcodeData(String barcode, String skuCode) {
        return skuBarcodeDataTransfer.toDTOs(skuMainDataRepository.findAllSkuBarcodeByBarcodeOrSkuCode(barcode, skuCode));
    }

    @Override
    public List<SkuBarcodeDataDTO> querySkuByBarcode(String barcode) {
        return skuBarcodeDataTransfer.toDTOs(skuMainDataRepository.findAllSkuBarcodeByBarcode(barcode));
    }

    @Override
    public SkuMainDataDTO getSkuMainData(String skuCode, String ownerCode) {
        SkuMainData skuMainData = skuMainDataRepository.findSkuBySkuCodeAndOwnerCode(skuCode, ownerCode);
        return skuMainDataTransfer.toDTO(skuMainData);
    }

    @Override
    public SkuMainDataDTO getAnySkuMainDataBySkuCode(String skuCode) {
        return skuMainDataTransfer.toDTO(skuMainDataRepository.findAnySkuMainDataBySkuCode(skuCode));
    }

    @Override
    public List<SkuMainDataDTO> getSkuMainData(Collection<String> skuCodes, String ownerCode) {
        List<SkuMainData> skuMainDataList = skuMainDataRepository.findAllSkuBySkuCodesAndOwnerCode(skuCodes, ownerCode);
        return skuMainDataTransfer.toDTOs(skuMainDataList);
    }

    @Override
    public List<SkuMainDataDTO> getByIds(Collection<Long> skuMainDataIds) {
        return skuMainDataTransfer.toDTOs(skuMainDataRepository.findAllByIds(skuMainDataIds));
    }

    @Override
    public SkuMainDataDTO getById(Long skuMainDataId) {
        return skuMainDataTransfer.toDTO(skuMainDataRepository.findById(skuMainDataId));
    }

    @Override
    public Page<SkuMainDataDTO> findAllByHeatIn(Collection<String> heat, PageRequest pageable) {
        return skuMainDataRepository.findAllByHeatIn(heat, pageable).map(skuMainDataTransfer::toDTO);
    }

    @Override
    public List<SkuMainDataDTO> findSkuMainDataBySkuCode(String skuCode) {
        return skuMainDataTransfer.toDTOs(skuMainDataRepository.findAllBySkuCode(skuCode));
    }
}
