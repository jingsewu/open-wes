package org.openwes.api.platform.application.service.handler;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RequestHandlerFactory implements InitializingBean {

    private static final Map<String, RequestHandler> map = new HashMap<>();

    @Autowired
    private List<RequestHandler> handlerList;

    @Override
    public void afterPropertiesSet() {
        for (RequestHandler handler : handlerList) {
            map.put(handler.getApiType(), handler);
        }
    }

    public RequestHandler getHandler(String apiType) {
        return map.get(apiType);
    }
}
