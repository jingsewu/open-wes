package org.openwes.api.platform.utils;

import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.openwes.api.platform.api.constants.ConverterTypeEnum;
import org.openwes.api.platform.domain.entity.ApiConfigPO;
import org.openwes.common.utils.utils.JsonUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Lazy
@Component
public class ConverterHelper {

    public static Object convertParam(ApiConfigPO apiConfigPO, Object dataObj) {
        if (apiConfigPO == null) {
            return dataObj;
        }
        ConverterTypeEnum type = apiConfigPO.getParamConverterType();
        if (type == null || type == ConverterTypeEnum.NONE) {
            return dataObj;
        }
        return convert(type, apiConfigPO.getParamConverterScript(), dataObj);
    }

    public static Object convertResponse(ApiConfigPO apiConfigPO, Object dataObj) {
        if (apiConfigPO == null) {
            return dataObj;
        }
        ConverterTypeEnum type = apiConfigPO.getResponseConverterType();
        if (type == null || type == ConverterTypeEnum.NONE) {
            return dataObj;
        }
        return convert(type, apiConfigPO.getResponseConverterScript(), dataObj);
    }

    private static Object convert(ConverterTypeEnum type, String script, Object dataObj) {
        return switch (type) {
            case JS -> convertWithJs(script, dataObj);
            case JAVA -> JavaScriptUtils.executeJava(script, dataObj);
            case TEMPLATE -> convertWithTemplate(script, dataObj);
            default -> dataObj;
        };
    }

    private static String convertWithJs(String script, Object obj) {
        try (Context context = Context.create()) {
            Object result = JavaScriptUtils.executeJs(context, script, obj);
            return JsonUtils.obj2String(result);
        }
    }

    private static Object convertWithTemplate(String script, Object dataObj) {
        return FreeMarkerHelper.convertByTemplate(
                script.getBytes(StandardCharsets.UTF_8), dataObj, (Map<String, Object>) null);
    }

    public static boolean isAsyncApi(String apiType, Integer count) {
        return false;
    }
}
