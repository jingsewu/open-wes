package org.openwes.wes.inbound.domain.entity;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openwes.wes.api.inbound.constants.PutAwayTaskStatusEnum;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PutAwayTaskTest extends BaseTest {

    private PutAwayTask putAwayTask;


    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        putAwayTask = new PutAwayTask();
        putAwayTask.initialize();
    }

    @Test
    void testInitialize_GeneratesTaskNoAndSetsStatusToNew() {
        assertNotNull(putAwayTask.getTaskNo(), "TaskNo should not be null after initialization");
        assertEquals(PutAwayTaskStatusEnum.NEW, putAwayTask.getTaskStatus(), "TaskStatus should be NEW after initialization");
    }


    @Test
    void testPutAwayTaskDetails_AddAndRetrieveDetails() {
        PutAwayTaskDetail detail = new PutAwayTaskDetail();
        detail.setSkuCode("SKU123");
        detail.setQtyPutAway(10);

        putAwayTask.setPutAwayTaskDetails(Lists.newArrayList(detail));

        assertEquals(1, putAwayTask.getPutAwayTaskDetails().size(), "PutAwayTaskDetails should contain one detail");
        assertEquals("SKU123", putAwayTask.getPutAwayTaskDetails().get(0).getSkuCode(), "SkuCode of the detail should be 'SKU123'");
    }

    @Test
    void testExtendFields_AddAndRetrieveCustomFields() {
        Map<String, Object> extendFields = new HashMap<>();
        extendFields.put("customField1", "value1");
        extendFields.put("customField2", "value2");

        putAwayTask.setExtendFields(extendFields);

        assertEquals("value1", putAwayTask.getExtendFields().get("customField1"), "ExtendFields should contain customField1 with value 'value1'");
        assertEquals("value2", putAwayTask.getExtendFields().get("customField2"), "ExtendFields should contain customField2 with value 'value2'");
    }
}
