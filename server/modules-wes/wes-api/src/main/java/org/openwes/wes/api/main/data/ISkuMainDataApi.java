package org.openwes.wes.api.main.data;

import org.openwes.wes.api.main.data.dto.SkuBarcodeDataDTO;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Collection;
import java.util.List;

public interface ISkuMainDataApi {

    void createOrUpdateBatch(@Valid List<SkuMainDataDTO> skuMainDataDTOs);

    List<SkuBarcodeDataDTO> querySkuBarcodeData(String barcode, String skuCode);

    List<SkuBarcodeDataDTO> querySkuByBarcode(String barcode);

    SkuMainDataDTO getSkuMainData(@NotEmpty String skuCode, @NotEmpty String ownerCode);

    SkuMainDataDTO getAnySkuMainDataBySkuCode(String skuCode);

    List<SkuMainDataDTO> getSkuMainData(@NotEmpty Collection<String> skuCodes, @NotEmpty String ownerCode);

    List<SkuMainDataDTO> getByIds(@NotEmpty Collection<Long> skuMainDataIds);

    SkuMainDataDTO getById(Long skuMainDataId);

    Page<SkuMainDataDTO> findAllByHeatIn(Collection<String> heat, PageRequest pageable);

    List<SkuMainDataDTO> findSkuMainDataBySkuCode(String skuCode);

}
