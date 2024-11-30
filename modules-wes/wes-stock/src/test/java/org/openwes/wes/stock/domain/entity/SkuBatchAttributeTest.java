package org.openwes.wes.stock.domain.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

class SkuBatchAttributeTest {

    @Mock
    private Map<String, Object> skuAttributes;

    private SkuBatchAttribute skuBatchAttribute;

    @BeforeEach
    void setUp() {
        skuBatchAttribute = new SkuBatchAttribute();
    }

    @Test
    void testIsSame_ReturnsTrue_WhenBothMapsAreEmpty() {
        // Arrange
        skuBatchAttribute.setId(1L);
        skuBatchAttribute.setSkuId(2L);
        skuBatchAttribute.setSkuAttributes(new HashMap<>());
        skuAttributes = new HashMap<>();

        // Act
        boolean result = skuBatchAttribute.isSame(skuAttributes);

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    void testIsSame_ReturnsTrue_WhenOtherMapIsEmpty() {
        // Arrange
        skuBatchAttribute.setId(1L);
        skuBatchAttribute.setSkuId(2L);
        skuBatchAttribute.setSkuAttributes(new HashMap<>());

        // Act
        boolean result = skuBatchAttribute.isSame(skuAttributes);

        // Assert
        Assertions.assertFalse(result);
    }

    @Test
    void testIsSame_ReturnsFalse_WhenOneMapIsEmpty() {
        // Arrange
        skuBatchAttribute.setId(1L);
        skuBatchAttribute.setSkuId(2L);
        skuBatchAttribute.setSkuAttributes(skuAttributes);
        skuAttributes = new HashMap<>();

        // Act
        boolean result = skuBatchAttribute.isSame(skuAttributes);

        // Assert
        Assertions.assertFalse(result);
    }

    @Test
    void testIsSame_ReturnsTrue_WhenMapsAreEqual() {
        // Arrange
        skuBatchAttribute.setId(1L);
        skuBatchAttribute.setSkuId(2L);
        skuBatchAttribute.setSkuAttributes(skuAttributes);
        skuAttributes.put("key", "value");
        skuBatchAttribute.getSkuAttributes().put("key", "value");

        // Act
        boolean result = skuBatchAttribute.isSame(skuAttributes);

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    void testGetBatchNo_ReturnsBatchNo_WhenBatchNoIsNotEmpty() {
        // Arrange
        skuBatchAttribute.setId(1L);
        skuBatchAttribute.setSkuId(2L);
        skuBatchAttribute.setBatchNo("BatchNo");

        // Act
        String result = skuBatchAttribute.getBatchNo();

        // Assert
        Assertions.assertEquals("BatchNo", result);
    }

    @Test
    void testGetBatchNo() {

        // Arrange
        Map<String, Object> batchMap1 = new HashMap<>();
        batchMap1.put("1", "1");
        batchMap1.put("2", "2");
        skuBatchAttribute.setId(1L);
        skuBatchAttribute.setSkuId(2L);
        skuBatchAttribute.setSkuAttributes(batchMap1);

        // Act
        String result = skuBatchAttribute.getBatchNo();

        Map<String, Object> batchMap2 = new HashMap<>();
        batchMap2.put("2", "2");
        batchMap2.put("1", "1");
        SkuBatchAttribute skuBatchAttribute2 = new SkuBatchAttribute(1L, batchMap2);

        // Assert
        Assertions.assertEquals(skuBatchAttribute2.getBatchNo(), result);
    }


}
