package org.openwes.api.platform.application.service;

import org.openwes.api.platform.api.dto.callback.CallbackMessage;
import org.openwes.api.platform.application.service.handler.CallbackHandler;
import org.openwes.api.platform.domain.entity.ApiPO;
import org.openwes.common.utils.http.Response;

import java.util.concurrent.CompletableFuture;

public interface HandlerExecutor {

    Object executeRequest(String apiType, String body);

    <T> CompletableFuture<Response> executeCallback(CallbackHandler handler, ApiPO apiPO, CallbackMessage<T> sourceData);

    <T> Response synchronizeExecuteCallback(CallbackHandler handler, ApiPO apiPO, CallbackMessage<T> sourceData);

    Response executeCallbackWithoutLog(CallbackHandler handler, ApiPO apiPO, Object sourceData);
}
