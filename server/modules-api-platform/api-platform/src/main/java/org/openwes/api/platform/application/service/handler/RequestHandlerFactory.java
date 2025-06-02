package org.openwes.api.platform.application.service.handler;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RequestHandlerFactory implements InitializingBean {

    private static final Map<String, AbstractRequestHandler> map = new HashMap<>();

    @Autowired
    private List<AbstractRequestHandler> handlerList;

    @Override
    public void afterPropertiesSet() {
        for (AbstractRequestHandler handler : handlerList) {
            map.put(handler.getApiType(), handler);
        }
    }

    public AbstractRequestHandler getHandler(String apiType) {
        return map.get(apiType);
    }
}
