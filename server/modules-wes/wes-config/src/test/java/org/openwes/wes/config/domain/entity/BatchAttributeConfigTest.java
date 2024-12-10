package org.openwes.wes.config.domain.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.openwes.wes.api.config.dto.BatchAttributeConfigDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

class BatchAttributeConfigTest {

    private BatchAttributeConfig batchAttributeConfig;
    private BatchAttributeConfigDTO.BatchAttributeFieldConfigDTO fieldConfigDTO;

    @BeforeEach
    void setUp() {
        fieldConfigDTO = new BatchAttributeConfigDTO.BatchAttributeFieldConfigDTO();
        fieldConfigDTO.setFieldCode("testCode");
        fieldConfigDTO.setFieldName("testName");
        fieldConfigDTO.setEnable(true);

        batchAttributeConfig = new BatchAttributeConfig();
        batchAttributeConfig.setId(1L);
        batchAttributeConfig.setCode("code");
        batchAttributeConfig.setName("name");
        batchAttributeConfig.setOwnerCode("ownerCode");
        batchAttributeConfig.setSkuFirstCategory("skuFirstCategory");
        batchAttributeConfig.setEnable(true);
        batchAttributeConfig.setVersion(1L);
        batchAttributeConfig.setBatchAttributeFieldConfigs(new ArrayList<>() {{
            add(fieldConfigDTO);
        }});
    }

    @Test
    void testMatch_BothPatternsMatch_ShouldReturnTrue() {
        assertTrue(batchAttributeConfig.match("ownerCode", "skuFirstCategory"));
    }

    @Test
    void testMatch_OwnerCodePatternMatch_ShouldReturnTrue() {
        batchAttributeConfig.setSkuFirstCategory("*");
        assertTrue(batchAttributeConfig.match("ownerCode", "anySkuFirstCategory"));
    }

    @Test
    void testMatch_SkuFirstCategoryPatternMatch_ShouldReturnTrue() {
        batchAttributeConfig.setOwnerCode("*");
        assertTrue(batchAttributeConfig.match("anyOwnerCode", "skuFirstCategory"));
    }

    @Test
    void testMatch_BothPatternsDoNotMatch_ShouldReturnFalse() {
        batchAttributeConfig.setOwnerCode("differentOwnerCode");
        batchAttributeConfig.setSkuFirstCategory("differentSkuFirstCategory");
        assertFalse(batchAttributeConfig.match("ownerCode", "skuFirstCategory"));
    }

    @Test
    void testMatchOwner_OwnerCodeMatches_ShouldReturnTrue() {
        assertTrue(batchAttributeConfig.matchOwner("ownerCode"));
    }

    @Test
    void testMatchOwner_OwnerCodeDoesNotMatch_ShouldReturnFalse() {
        batchAttributeConfig.setOwnerCode("differentOwnerCode");
        assertFalse(batchAttributeConfig.matchOwner("ownerCode"));
    }

    @Test
    void testMatchOwner_EitherIsWildcard_ShouldReturnTrue() {
        batchAttributeConfig.setOwnerCode("*");
        assertTrue(batchAttributeConfig.matchOwner("anyOwnerCode"));
        batchAttributeConfig.setOwnerCode("ownerCode");
        assertTrue(batchAttributeConfig.matchOwner("*"));
    }

    @Test
    void testMatchSkuFirstCategory_SkuFirstCategoryMatches_ShouldReturnTrue() {
        assertTrue(batchAttributeConfig.matchSkuFirstCategory("skuFirstCategory"));
    }

    @Test
    void testMatchSkuFirstCategory_SkuFirstCategoryDoesNotMatch_ShouldReturnFalse() {
        batchAttributeConfig.setSkuFirstCategory("differentSkuFirstCategory");
        assertFalse(batchAttributeConfig.matchSkuFirstCategory("skuFirstCategory"));
    }

    @Test
    void testMatchSkuFirstCategory_EitherIsWildcard_ShouldReturnTrue() {
        batchAttributeConfig.setSkuFirstCategory("*");
        assertTrue(batchAttributeConfig.matchSkuFirstCategory("anySkuFirstCategory"));
        batchAttributeConfig.setSkuFirstCategory("skuFirstCategory");
        assertTrue(batchAttributeConfig.matchSkuFirstCategory("*"));
    }
}
