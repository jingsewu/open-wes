package org.openwes.wes.basic.main.data.domain.aggregate;

import org.openwes.wes.api.main.data.dto.BarcodeDTO;
import org.openwes.wes.basic.main.data.domain.entity.SkuBarcodeData;
import org.openwes.wes.basic.main.data.domain.entity.SkuMainData;
import org.openwes.wes.basic.main.data.domain.repository.SkuMainDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkuMainDataAggregateTest {

    @Mock
    private SkuMainDataRepository skuMainDataRepository;

    @InjectMocks
    private SkuMainDataAggregate skuMainDataAggregate;

    @BeforeEach
    public void setUp() {
    }

    @Test
    void create_ValidInput_ShouldProcessCorrectly() {
        // Arrange
        List<SkuBarcodeData> skuBarcodeDataList = new ArrayList<>();
        SkuBarcodeData barcode1 = new SkuBarcodeData();
        barcode1.setSkuId(1L);
        barcode1.setBarCode("123");
        skuBarcodeDataList.add(barcode1);

        List<SkuMainData> changedSkuMainDataList = new ArrayList<>();
        SkuMainData sku1 = new SkuMainData();
        sku1.setId(1L);
        sku1.setSkuBarcode(new BarcodeDTO("123"));
        changedSkuMainDataList.add(sku1);

        // Act
        skuMainDataAggregate.create(changedSkuMainDataList, skuBarcodeDataList);

        // Assert
        verify(skuMainDataRepository, times(1)).saveAll(changedSkuMainDataList);
        verify(skuMainDataRepository, never()).deleteAllSkuBarcodeData(anyList());
    }

    @Test
    void create_WithUselessBarcodes_ShouldDeleteThem() {
        // Arrange
        List<SkuMainData> changedSkuMainDataList = new ArrayList<>();
        SkuMainData sku1 = new SkuMainData();
        sku1.setId(1L);
        changedSkuMainDataList.add(sku1);

        List<SkuBarcodeData> skuBarcodeDataList = new ArrayList<>();
        SkuBarcodeData barcode1 = new SkuBarcodeData();
        barcode1.setSkuId(1L);
        barcode1.setBarCode("123");
        skuBarcodeDataList.add(barcode1);
        SkuBarcodeData barcode2 = new SkuBarcodeData();
        barcode2.setSkuId(1L);
        barcode2.setBarCode("456");
        skuBarcodeDataList.add(barcode2);

        sku1.setSkuBarcode(new BarcodeDTO("123")); // existing barcode
        sku1.setSkuBarcode(new BarcodeDTO("789")); // another existing barcode

        // Act
        skuMainDataAggregate.create(changedSkuMainDataList, skuBarcodeDataList);

        // Assert
        verify(skuMainDataRepository, times(1)).deleteAllSkuBarcodeData(anyList());
    }
}
