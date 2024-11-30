package org.openwes.wes.basic.main.data.domain.entity;

import org.openwes.wes.api.main.data.dto.BarcodeDTO;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SkuMainDataTest {

    private SkuMainData skuMainData;
    private SkuMainDataDTO skuMainDataDTO;

    @BeforeEach
    void setUp() {
        skuMainData = new SkuMainData();
        skuMainData.setSkuCode("testSkuCode");
        skuMainData.setWarehouseCode("testWarehouseCode");
        skuMainData.setOwnerCode("testOwnerCode");

        skuMainDataDTO = new SkuMainDataDTO();
        skuMainDataDTO.setSkuCode("testSkuCode");
        skuMainDataDTO.setWarehouseCode("testWarehouseCode");
        skuMainDataDTO.setOwnerCode("testOwnerCode");
    }

    @Test
    void equalsDto_ValidInput_ReturnsTrue() {
        assertTrue(skuMainData.equalsDto(skuMainDataDTO));
    }

    @Test
    void equalsDto_NullInput_ReturnsFalse() {
        assertFalse(skuMainData.equalsDto(null));
    }

    @Test
    void equalsDto_DifferentFields_ReturnsFalse() {
        skuMainDataDTO.setSkuCode("differentSkuCode");
        assertFalse(skuMainData.equalsDto(skuMainDataDTO));
    }

    @Test
    void ignoreBarcodeData_EmptyInput_DoesNothing() {
        List<SkuBarcodeData> emptyList = new ArrayList<>();
        skuMainData.setSkuBarcode(new BarcodeDTO());
        skuMainData.getSkuBarcode().setBarcodes(new ArrayList<>());
        skuMainData.getSkuBarcode().getBarcodes().add("existingBarcode");

        skuMainData.ignoreBarcodeData(emptyList);

        assertEquals(1, skuMainData.getSkuBarcode().getBarcodes().size());
        assertTrue(skuMainData.getSkuBarcode().getBarcodes().contains("existingBarcode"));
    }

    @Test
    void ignoreBarcodeData_ValidInput_RemovesBarcodes() {
        List<SkuBarcodeData> barcodeDataList = new ArrayList<>();
        SkuBarcodeData skuBarcodeData = new SkuBarcodeData();
        skuBarcodeData.setBarCode("barcodeToRemove");
        barcodeDataList.add(skuBarcodeData);
        skuMainData.setSkuBarcode(new BarcodeDTO());
        skuMainData.getSkuBarcode().setBarcodes(new ArrayList<>());
        skuMainData.getSkuBarcode().getBarcodes().add("existingBarcode");
        skuMainData.getSkuBarcode().getBarcodes().add("barcodeToRemove");

        skuMainData.ignoreBarcodeData(barcodeDataList);

        assertEquals(1, skuMainData.getSkuBarcode().getBarcodes().size());
        assertFalse(skuMainData.getSkuBarcode().getBarcodes().contains("barcodeToRemove"));
    }

    @Test
    void containsBarcode_ValidInput_ReturnsTrue() {
        skuMainData.setSkuBarcode(new BarcodeDTO());
        skuMainData.getSkuBarcode().setBarcodes(new ArrayList<>());
        skuMainData.getSkuBarcode().getBarcodes().add("testBarcode");

        assertTrue(skuMainData.containsBarcode("testBarcode"));
    }

    @Test
    void containsBarcode_NotContains_ReturnsFalse() {
        skuMainData.setSkuBarcode(new BarcodeDTO());
        skuMainData.getSkuBarcode().setBarcodes(new ArrayList<>());
        skuMainData.getSkuBarcode().getBarcodes().add("existingBarcode");

        assertFalse(skuMainData.containsBarcode("nonExistingBarcode"));
    }

    @Test
    void containsBarcode_EmptyBarcodes_ReturnsFalse() {
        skuMainData.setSkuBarcode(new BarcodeDTO());
        skuMainData.getSkuBarcode().setBarcodes(new ArrayList<>());

        assertFalse(skuMainData.containsBarcode("testBarcode"));
    }

    @Test
    void containsBarcode_NullSkuBarcode_ReturnsFalse() {
        skuMainData.setSkuBarcode(null);

        assertFalse(skuMainData.containsBarcode("testBarcode"));
    }
}
