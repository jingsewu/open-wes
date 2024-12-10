package org.openwes.api.platform.api;

import org.openwes.api.platform.api.constants.CallbackApiTypeEnum;
import org.openwes.api.platform.api.dto.callback.CallbackMessage;
import org.openwes.common.utils.http.Response;

public interface ICallbackApi {

    <T> Response callback(CallbackApiTypeEnum callbackType, String bizType, CallbackMessage<T> sourceData);
}
