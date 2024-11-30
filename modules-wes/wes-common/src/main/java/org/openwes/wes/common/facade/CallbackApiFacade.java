package org.openwes.wes.common.facade;

import org.openwes.api.platform.api.ICallbackApi;
import org.openwes.api.platform.api.constants.CallbackApiTypeEnum;
import org.openwes.api.platform.api.dto.callback.CallbackMessage;
import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.ems.proxy.constants.ContainerTaskTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class CallbackApiFacade {

    private final ICallbackApi callbackApi;

    public <T> Response callback(CallbackApiTypeEnum callbackType, String bizType, Object sourceData) {

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    callbackApi.callback(callbackType, bizType, new CallbackMessage<>().setData(sourceData));
                }
            });
        } else {
            callbackApi.callback(callbackType, bizType, new CallbackMessage<>().setData(sourceData));
        }

        return callbackApi.callback(callbackType, bizType, new CallbackMessage<>().setData(sourceData));
    }

    public void callback(CallbackApiTypeEnum callbackType, Object sourceData, ContainerTaskTypeEnum bizType) {
        String bizTypeName = bizType == null ? null : bizType.name();

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    callbackApi.callback(callbackType, bizTypeName, new CallbackMessage<>().setData(sourceData));
                }
            });
        } else {
            callbackApi.callback(callbackType, bizTypeName, new CallbackMessage<>().setData(sourceData));
        }
    }

}
