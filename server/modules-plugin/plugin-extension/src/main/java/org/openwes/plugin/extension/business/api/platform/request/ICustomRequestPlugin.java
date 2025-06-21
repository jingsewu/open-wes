package org.openwes.plugin.extension.business.api.platform.request;

import org.openwes.api.platform.api.constants.ApiTypeEnum;
import org.openwes.api.platform.api.dto.request.RequestHandleContext;
import org.openwes.plugin.extension.IPlugin;

/**
 * custom request plugin, user can implement this interface to handle custom request.
 * frequently used to extend the platform to support custom request
 */
public interface ICustomRequestPlugin extends IPlugin, IRequestPlugin {

    /**
     * the API type
     *
     * @return
     * @see org.openwes.api.platform.api.constants.ApiTypeEnum
     */
    default String getApiType() {
        return ApiTypeEnum.CUSTOM_API.name();
    }

    /**
     * invoke the api, custom call remote api or do any things
     *
     * @param context
     */
    default void invoke(RequestHandleContext context) {
    }

}
