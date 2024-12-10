package org.openwes.wes.config.domain.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import org.openwes.wes.api.config.constants.BusinessFlowEnum;
import org.openwes.wes.api.config.constants.ExecuteTimeEnum;
import org.openwes.wes.api.config.constants.UnionLocationEnum;
import org.openwes.wes.api.config.dto.BarcodeParseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class BarcodeParseRuleTest {

    private BarcodeParseRule barcodeParseRule;

    @BeforeEach
    public void setUp() {
        barcodeParseRule = new BarcodeParseRule();
        barcodeParseRule.setId(1L);
        barcodeParseRule.setCode("code");
        barcodeParseRule.setName("name");
        barcodeParseRule.setOwnerCode("ownerCode");
        barcodeParseRule.setExecuteTime(ExecuteTimeEnum.SCAN_CONTAINER);
        barcodeParseRule.setBusinessFlow(BusinessFlowEnum.OUTBOUND);
        barcodeParseRule.setBrand("brand");
        barcodeParseRule.setEnable(true);
        barcodeParseRule.setUnionLocation(UnionLocationEnum.LEFT);
        barcodeParseRule.setUnionStr("123");
        barcodeParseRule.setRegularExpression("(\\d+)(\\w+)");
        barcodeParseRule.setResultFields(Lists.newArrayList("field1", "field2"));
        barcodeParseRule.setVersion(1L);
    }

    @Test
    void testParse_ValidBarcode_ReturnsExpectedResults() {
        String barcode = "4545454545454";
        List<BarcodeParseResult> results = barcodeParseRule.parse(barcode);

        assertEquals(2, results.size());
        assertEquals("field1", results.get(0).getFieldName());
        assertEquals("123454545454545", results.get(0).getFieldValue());
        assertEquals("field2", results.get(1).getFieldName());
        assertEquals("4", results.get(1).getFieldValue());
    }

    @Test
    void testParse_InvalidBarcode_ReturnsEmptyList() {
        barcodeParseRule.setRegularExpression("(\\w+)");
        String barcode = "4545454545454";
        List<BarcodeParseResult> results = barcodeParseRule.parse(barcode);

        assertTrue(results.isEmpty());
    }

    @Test
    void testParse_EmptyResultFields_ReturnsEmptyList() {
        barcodeParseRule.setResultFields(Lists.newArrayList());
        String barcode = "4545454545454";
        List<BarcodeParseResult> results = barcodeParseRule.parse(barcode);

        assertTrue(results.isEmpty());
    }

    @Test
    void testMatchOwner_OwnerCodeMatches_ReturnsTrue() {
        boolean result = barcodeParseRule.matchOwner("ownerCode");

        assertTrue(result);
    }

    @Test
    void testMatchOwner_OwnerCodeIsWildcard_ReturnsTrue() {
        barcodeParseRule.setOwnerCode("*");
        boolean result = barcodeParseRule.matchOwner("anyOwnerCode");

        assertTrue(result);
    }

    @Test
    void testMatchBrand_BrandMatches_ReturnsTrue() {
        boolean result = barcodeParseRule.matchBrand("brand");

        assertTrue(result);
    }

    @Test
    void testMatchBrand_BrandIsWildcard_ReturnsTrue() {
        barcodeParseRule.setBrand("*");
        boolean result = barcodeParseRule.matchBrand("anyBrand");

        assertTrue(result);
    }
}
