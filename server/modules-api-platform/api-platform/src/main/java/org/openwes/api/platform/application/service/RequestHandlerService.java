package org.openwes.api.platform.application.service;

import org.openwes.api.platform.api.IRequestExtensionApi;
import org.openwes.api.platform.api.dto.request.RequestHandleContext;

import java.util.List;

public interface RequestHandlerService extends IRequestExtensionApi {

    void convertParam(RequestHandleContext context);

    Object response(RequestHandleContext context);

    <T> List<T> getTargetList(RequestHandleContext context, Class<T> clz);
}
