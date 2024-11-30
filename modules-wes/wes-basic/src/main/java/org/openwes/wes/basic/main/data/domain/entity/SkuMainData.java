package org.openwes.wes.basic.main.data.domain.entity;

import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.openwes.wes.api.main.data.dto.*;

import java.util.List;
import java.util.Objects;


@Data
public class SkuMainData {

    private Long id;

    /**
     * skuCode + ownerCode + warehouseCode union unique identifier
     */
    private String skuCode;
    private String warehouseCode;
    private String ownerCode;
    private String skuName;
    private String style;
    private String color;
    private String size;
    private String brand;
    private boolean suit;

    private WeightDTO weight;
    private VolumeDTO volumeDTO;

    private SkuAttributeDTO skuAttribute;
    private SkuConfigDTO skuConfig;
    private BarcodeDTO skuBarcode;

    private Long version;

    public boolean equalsDto(SkuMainDataDTO skuDto) {
        if (skuDto == null) {
            return false;
        }
        return Objects.equals(this.skuCode, skuDto.getSkuCode())
                && Objects.equals(this.warehouseCode, skuDto.getWarehouseCode())
                && Objects.equals(this.ownerCode, skuDto.getOwnerCode());
    }

    public void ignoreBarcodeData(List<SkuBarcodeData> barcodeData) {

        if (ObjectUtils.isEmpty(barcodeData)) {
            return;
        }

        if (skuBarcode == null || ObjectUtils.isEmpty(skuBarcode.getBarcodes())) {
            return;
        }
        skuBarcode.getBarcodes().removeAll(barcodeData.stream().map(SkuBarcodeData::getBarCode).toList());
    }

    public boolean containsBarcode(String barCode) {

        if (skuBarcode == null || ObjectUtils.isEmpty(skuBarcode.getBarcodes())) {
            return false;
        }
        return skuBarcode.getBarcodes().contains(barCode);
    }
}
