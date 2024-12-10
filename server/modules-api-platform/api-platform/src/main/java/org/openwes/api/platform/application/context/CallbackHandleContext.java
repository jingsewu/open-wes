package org.openwes.api.platform.application.context;

import org.openwes.api.platform.domain.entity.ApiConfigPO;
import org.openwes.api.platform.domain.entity.ApiPO;
import org.openwes.common.utils.http.Response;
import lombok.Data;

@Data
public class CallbackHandleContext {

    private ApiPO apiPO;
    private ApiConfigPO apiConfig;

    private Object sourceData;

    /**
     * 是否忽略回传，即无需回传
     */
    private boolean isIgnoreCallback;

    private Object targetData;

    private Response response;
}
