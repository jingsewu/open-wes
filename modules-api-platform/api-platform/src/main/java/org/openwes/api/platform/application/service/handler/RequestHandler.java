package org.openwes.api.platform.application.service.handler;

import com.alibaba.fastjson2.JSONArray;
import org.openwes.api.platform.application.context.RequestHandleContext;
import org.openwes.api.platform.application.service.RequestHandlerService;
import org.openwes.api.platform.infrastructure.WmsClientService;
import org.openwes.api.platform.utils.CommonUtils;
import org.openwes.api.platform.utils.ConverterHelper;
import org.openwes.common.utils.utils.JsonUtils;
import org.openwes.common.utils.utils.ValidatorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public abstract class RequestHandler implements RequestHandlerService {

    @Autowired
    protected WmsClientService coreClientService;

    @Override
    public void convertParam(RequestHandleContext context) {
        JSONArray jsonArray = CommonUtils.parseBody(context.getBody());
        assert jsonArray != null;
        List<Object> targetList = new ArrayList<>(jsonArray.size());
        jsonArray.forEach(obj -> {
            Object targetObj = ConverterHelper.convertParam(context.getApiConfig(), obj);
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
    }

    @Override
    public void supply(RequestHandleContext context) {
    }

    @Override
    public void saveData(RequestHandleContext context) {
    }

    @Override
    public void afterInvoke(RequestHandleContext context) {
    }

    @Override
    public Object response(RequestHandleContext context) {
        return ConverterHelper.convertResponse(context.getApiConfig(), context.getResponse());
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
