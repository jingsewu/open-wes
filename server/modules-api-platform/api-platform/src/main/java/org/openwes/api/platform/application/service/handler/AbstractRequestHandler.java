package org.openwes.api.platform.application.service.handler;

import com.alibaba.fastjson2.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.openwes.api.platform.api.dto.request.RequestHandleContext;
import org.openwes.api.platform.application.service.RequestHandlerService;
import org.openwes.api.platform.domain.transfer.ApiConfigTransfer;
import org.openwes.api.platform.infrastructure.WmsClientService;
import org.openwes.api.platform.utils.CommonUtils;
import org.openwes.api.platform.utils.ConverterHelper;
import org.openwes.common.utils.utils.JsonUtils;
import org.openwes.common.utils.utils.ValidatorUtils;
import org.openwes.plugin.extension.business.api.platform.request.ICustomRequestPlugin;
import org.openwes.plugin.extension.business.api.platform.request.IRequestPlugin;
import org.openwes.plugin.sdk.utils.PluginSdkUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public abstract class AbstractRequestHandler implements RequestHandlerService {

    @Autowired
    protected WmsClientService coreClientService;

    @Autowired
    protected ApiConfigTransfer apiConfigTransfer;

    @Autowired
    private PluginSdkUtils pluginUtils;

    @Override
    public void convertParam(RequestHandleContext context) {
        JSONArray jsonArray = CommonUtils.parseBody(context.getBody());
        assert jsonArray != null;
        List<Object> targetList = new ArrayList<>(jsonArray.size());
        jsonArray.forEach(obj -> {
            Object targetObj = ConverterHelper.convertParam(apiConfigTransfer.toDO(context.getApiConfig()), obj);
            if (targetObj instanceof JSONArray arrayObject) {
                targetList.addAll(arrayObject);
            } else {
                targetList.add(targetObj);
            }
        });
        context.setTargetList(targetList);
    }

    @Override
    public void validate(RequestHandleContext context) {

        List<IRequestPlugin> requestExtensionApis = pluginUtils.getExtractObject(IRequestPlugin.class);
        if (ObjectUtils.isEmpty(requestExtensionApis)) {
            return;
        }

        requestExtensionApis.stream().filter(v -> StringUtils.equals(v.getApiType(), this.getApiType()))
                .forEach(v -> v.validate(context));
    }

    @Override
    public void supply(RequestHandleContext context) {

        List<IRequestPlugin> requestExtensionApis = pluginUtils.getExtractObject(IRequestPlugin.class);
        if (ObjectUtils.isEmpty(requestExtensionApis)) {
            return;
        }

        requestExtensionApis.stream().filter(v -> StringUtils.equals(v.getApiType(), this.getApiType()))
                .forEach(v -> v.supply(context));
    }

    @Override
    public void invoke(RequestHandleContext context) {

        List<ICustomRequestPlugin> requestExtensionApis = pluginUtils.getExtractObject(ICustomRequestPlugin.class);
        if (ObjectUtils.isEmpty(requestExtensionApis)) {
            return;
        }

        requestExtensionApis.stream().filter(v -> StringUtils.equals(v.getApiType(), this.getApiType()))
                .forEach(v -> v.invoke(context));
    }

    @Override
    public void afterInvoke(RequestHandleContext context) {

        List<IRequestPlugin> requestExtensionApis = pluginUtils.getExtractObject(IRequestPlugin.class);
        if (ObjectUtils.isEmpty(requestExtensionApis)) {
            return;
        }

        requestExtensionApis.stream().filter(v -> StringUtils.equals(v.getApiType(), this.getApiType()))
                .forEach(v -> v.afterInvoke(context));
    }

    @Override
    public Object response(RequestHandleContext context) {
        return ConverterHelper.convertResponse(apiConfigTransfer.toDO(context.getApiConfig()), context.getResponse());
    }

    @Override
    public <T> List<T> getTargetList(RequestHandleContext context, Class<T> clz) {
        if (CollectionUtils.isEmpty(context.getTargetList())) {
            return Collections.emptyList();
        }

        return context.getTargetList().stream().filter(Objects::nonNull).map(target -> {
            T t;
            if (target instanceof String string) {
                t = JsonUtils.string2Object(string, clz);
            } else {
                t = JsonUtils.string2Object(JsonUtils.obj2String(target), clz);
            }
            ValidatorUtils.validate(t);
            return t;
        }).toList();

    }
}
