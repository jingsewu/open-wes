package org.openwes.api.platform.application.service;

import org.openwes.api.platform.api.dto.callback.CallbackMessage;
import org.openwes.api.platform.application.service.handler.AbstractCallbackHandler;
import org.openwes.api.platform.domain.entity.ApiPO;
import org.openwes.common.utils.http.Response;

public interface CallbackExecutor {

    <T> Response synchronizeExecuteCallback(AbstractCallbackHandler handler, ApiPO apiPO, CallbackMessage<T> sourceData);

    Response executeCallbackWithoutLog(AbstractCallbackHandler handler, ApiPO apiPO, Object sourceData);
}
