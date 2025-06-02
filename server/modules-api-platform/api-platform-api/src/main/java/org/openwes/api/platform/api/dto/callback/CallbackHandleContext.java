package org.openwes.api.platform.api.dto.callback;

import lombok.Data;
import org.openwes.api.platform.api.dto.api.ApiConfigDTO;
import org.openwes.api.platform.api.dto.api.ApiDTO;
import org.openwes.common.utils.http.Response;

@Data
public class CallbackHandleContext {

    private ApiDTO api;
    private ApiConfigDTO apiConfig;

    private Object sourceData;

    /**
     * 是否忽略回传，即无需回传
     */
    private boolean isIgnoreCallback;

    private Object targetData;

    private Response response;
}
