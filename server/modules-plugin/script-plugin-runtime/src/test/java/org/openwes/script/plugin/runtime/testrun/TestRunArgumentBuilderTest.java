package org.openwes.script.plugin.runtime.testrun;

import org.junit.jupiter.api.Test;
import org.openwes.api.platform.api.dto.request.RequestHandleContext;
import org.openwes.plugin.extension.business.station.CustomApiParameter;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderDTO;
import org.openwes.wes.api.outbound.dto.OutboundWaveDTO;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestRunArgumentBuilderTest {

    @Test
    void httpEndpoint_passesBodyThrough() {
        Object[] arguments = TestRunArgumentBuilder.build(ExtensionType.HTTP_ENDPOINT,
                Map.of("body", Map.of("orderNo", "SO-1")));

        assertEquals(1, arguments.length);
        assertEquals(Map.of("orderNo", "SO-1"), arguments[0]);
    }

    @Test
    void cron_hasNoArguments() {
        assertEquals(0, TestRunArgumentBuilder.build(ExtensionType.CRON, Map.of()).length);
    }

    @Test
    void lifecycleListener_buildsThreeTypedValues() {
        Object[] arguments = TestRunArgumentBuilder.build(ExtensionType.LIFECYCLE_LISTENER,
                Map.of("entityId", 42, "entityOrderNo", "SO-1", "newStatus", "DONE"));

        assertEquals(3, arguments.length);
        assertEquals(42L, arguments[0]);
        assertEquals("SO-1", arguments[1]);
        assertEquals("DONE", arguments[2]);
    }

    @Test
    void outboundWaveAction_convertsOrdersToHostDtos() {
        Object[] arguments = TestRunArgumentBuilder.build(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION,
                Map.of("orders", List.of(Map.of("customerOrderNo", "SO-1", "warehouseCode", "WH1"))));

        List<?> orders = assertInstanceOf(List.class, arguments[0]);
        OutboundPlanOrderDTO order = assertInstanceOf(OutboundPlanOrderDTO.class, orders.get(0));
        assertEquals("SO-1", order.getCustomerOrderNo());
        assertEquals("WH1", order.getWarehouseCode());
    }

    @Test
    void outboundWaveSplitAction_convertsWaveToHostDto() {
        Object[] arguments = TestRunArgumentBuilder.build(ExtensionType.OUTBOUND_WAVE_SPLIT_ACTION,
                Map.of("wave", Map.of("waveNo", "W-1", "warehouseCode", "WH1")));

        OutboundWaveDTO wave = assertInstanceOf(OutboundWaveDTO.class, arguments[0]);
        assertEquals("W-1", wave.getWaveNo());
        assertEquals("WH1", wave.getWarehouseCode());
    }

    @Test
    void requestArchetypes_convertTheRequestContext() {
        for (ExtensionType type : List.of(ExtensionType.REQUEST_VALIDATE,
                ExtensionType.REQUEST_SUPPLY, ExtensionType.REQUEST_AFTER_INVOKE,
                ExtensionType.CUSTOM_REQUEST_INVOKE)) {
            Object[] arguments = TestRunArgumentBuilder.build(type,
                    Map.of("context", Map.of("apiType", "outbound", "body", "{}")));

            RequestHandleContext context = assertInstanceOf(RequestHandleContext.class, arguments[0]);
            assertEquals("outbound", context.getApiType());
            assertEquals("{}", context.getBody());
        }
    }

    @Test
    void customApi_constructsTheParameterFromNamedFields() {
        Object[] arguments = TestRunArgumentBuilder.build(ExtensionType.CUSTOM_API,
                Map.of("parameter", Map.of("workStationId", 9,
                        "requestParameter", Map.of("code", "ABC"))));

        CustomApiParameter parameter = assertInstanceOf(CustomApiParameter.class, arguments[0]);
        assertEquals(9L, parameter.getWorkStationId());
        assertInstanceOf(Map.class, parameter.getRequestParameter());
    }

    @Test
    void missingArguments_becomeNullArguments() {
        Object[] arguments = TestRunArgumentBuilder.build(ExtensionType.LIFECYCLE_LISTENER, Map.of());

        assertEquals(3, arguments.length);
        assertTrue(arguments[0] == null && arguments[1] == null && arguments[2] == null);
    }
}
