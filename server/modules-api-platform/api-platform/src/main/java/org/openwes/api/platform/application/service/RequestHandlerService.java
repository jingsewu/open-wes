package org.openwes.api.platform.application.service;

import org.openwes.api.platform.application.context.RequestHandleContext;

import java.util.List;

public interface RequestHandlerService {

    String getApiType();

    void convertParam(RequestHandleContext context);

    void validate(RequestHandleContext context);

    void supply(RequestHandleContext context);

    void saveData(RequestHandleContext context);

    void invoke(RequestHandleContext context);

    void afterInvoke(RequestHandleContext context);

    Object response(RequestHandleContext context);

    <T> List<T> getTargetList(RequestHandleContext context, Class<T> clz);
}
