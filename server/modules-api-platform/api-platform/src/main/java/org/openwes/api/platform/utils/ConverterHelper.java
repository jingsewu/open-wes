package org.openwes.api.platform.utils;

import org.openwes.api.platform.api.constants.ConverterTypeEnum;
import org.openwes.api.platform.domain.entity.ApiConfigPO;
import org.openwes.common.utils.utils.JsonUtils;
import org.openwes.distribute.file.client.FastdfsClient;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Lazy
@Component
public class ConverterHelper {
    private static FastdfsClient fastdfsClient;


    @Autowired
    public ConverterHelper(FastdfsClient fastdfsClient) {
        ConverterHelper.fastdfsClient = fastdfsClient;
    }

    public static Object convertParam(ApiConfigPO apiConfigPO, Object dataObj) {
        if (apiConfigPO == null) {
            return dataObj;
        }

        if (apiConfigPO.getParamConverterType() == null || apiConfigPO.getParamConverterType() == ConverterTypeEnum.NONE) {
            return dataObj;
        }

        if (apiConfigPO.getParamConverterType() == ConverterTypeEnum.JS) {
            return convertParamWithJsConverter(apiConfigPO.getJsParamConverter(), dataObj);
        }

        return convertParamWithTemplateConverter(apiConfigPO.getTemplateParamConverter(), dataObj);
    }

    public static Object convertResponse(ApiConfigPO apiConfigPO, Object dataObj) {

        if (apiConfigPO == null) {
            return dataObj;
        }

        if (apiConfigPO.getResponseConverterType() == null || apiConfigPO.getResponseConverterType() == ConverterTypeEnum.NONE) {
            return dataObj;
        }

        if (apiConfigPO.getResponseConverterType() == ConverterTypeEnum.JS) {
            return convertParamWithJsConverter(apiConfigPO.getJsResponseConverter(), dataObj);
        }

        return convertParamWithTemplateConverter(apiConfigPO.getTemplateResponseConverter(), dataObj);
    }

    private static String convertParamWithJsConverter(String jsScript, Object obj) {

        try (Context context = Context.create()) {
            Object result = JavaScriptUtils.executeJs(context, jsScript, obj);
            return JsonUtils.obj2String(result);
        }

    }

    private static Object convertParamWithTemplateConverter(String templateUrl, Object dataObj) {
        byte[] templateFile = fastdfsClient.download(templateUrl);
        return FreeMarkerHelper.convertByTemplate(templateFile, dataObj, (Map<String, Object>) null);
    }

    /**
     * 是否开启异步执行
     *
     * @param apiType
     * @return
     */
    public static boolean isAsyncApi(String apiType, Integer count) {
        return false;
    }

}
