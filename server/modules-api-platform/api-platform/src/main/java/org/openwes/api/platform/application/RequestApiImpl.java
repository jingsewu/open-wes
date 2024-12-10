package org.openwes.api.platform.application;

import org.openwes.api.platform.api.IRequestApi;
import org.openwes.api.platform.application.service.HandlerExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestApiImpl implements IRequestApi {

    private final HandlerExecutor handlerExecutor;

    @Override
    public Object request(String apiType, String body) {
        return handlerExecutor.executeRequest(apiType, body);
    }
}
