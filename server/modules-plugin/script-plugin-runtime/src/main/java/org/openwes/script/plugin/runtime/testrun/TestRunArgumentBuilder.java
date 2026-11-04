package org.openwes.script.plugin.runtime.testrun;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openwes.api.platform.api.dto.request.RequestHandleContext;
import org.openwes.plugin.extension.business.station.CustomApiParameter;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderDTO;
import org.openwes.wes.api.outbound.dto.OutboundWaveDTO;

import java.util.List;
import java.util.Map;

/**
 * Maps the user's named JSON test arguments to the host signature. The returned array does not
 * include the trailing ctx; the runner appends it.
 */
public final class TestRunArgumentBuilder {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private TestRunArgumentBuilder() {
    }

    public static Object[] build(ExtensionType type, Map<String, Object> arguments) {
        Map<String, Object> args = arguments == null ? Map.of() : arguments;
        return switch (type) {
            case HTTP_ENDPOINT -> new Object[]{args.get("body")};
            case CRON -> new Object[0];
            case LIFECYCLE_LISTENER -> new Object[]{
                    convert(args.get("entityId"), Long.class),
                    convert(args.get("entityOrderNo"), String.class),
                    convert(args.get("newStatus"), String.class)};
            case OUTBOUND_WAVE_WAVE_ACTION -> new Object[]{
                    convertList(args.get("orders"), OutboundPlanOrderDTO.class)};
            case OUTBOUND_WAVE_SPLIT_ACTION -> new Object[]{
                    convert(args.get("wave"), OutboundWaveDTO.class)};
            case REQUEST_VALIDATE, REQUEST_SUPPLY, REQUEST_AFTER_INVOKE, CUSTOM_REQUEST_INVOKE ->
                    new Object[]{convert(args.get("context"), RequestHandleContext.class)};
            case CUSTOM_API -> new Object[]{customApiParameter(args.get("parameter"))};
        };
    }

    private static CustomApiParameter customApiParameter(Object value) {
        if (value == null) {
            return new CustomApiParameter(null, null);
        }
        Map<String, Object> map = OBJECT_MAPPER.convertValue(value, new TypeReference<>() {
        });
        Long workStationId = map.get("workStationId") == null
                ? null : OBJECT_MAPPER.convertValue(map.get("workStationId"), Long.class);
        return new CustomApiParameter(workStationId, map.get("requestParameter"));
    }

    private static <T> T convert(Object value, Class<T> type) {
        return value == null ? null : OBJECT_MAPPER.convertValue(value, type);
    }

    private static <T> List<T> convertList(Object value, Class<T> elementType) {
        if (value == null) {
            return List.of();
        }
        JavaType listType = OBJECT_MAPPER.getTypeFactory()
                .constructCollectionType(List.class, elementType);
        return OBJECT_MAPPER.convertValue(value, listType);
    }
}
