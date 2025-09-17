package org.openwes.plugin.extension.business.api.platform.request;

import org.openwes.api.platform.api.dto.request.RequestHandleContext;
import org.openwes.plugin.extension.IPlugin;

public interface IRequestPlugin extends IPlugin {

    /**
     * the API type
     *
     * @return
     * @see org.openwes.api.platform.api.constants.ApiTypeEnum
     */
    String getApiType();

    /**
     * validate the context is illegal
     *
     * @param context
     */
    default void validate(RequestHandleContext context) {
    }


    /**
     * supply the context or do something before invoke
     *
     * @param context
     */
    default void supply(RequestHandleContext context) {
    }

    /**
     * do something after invoke
     *
     * @param context
     */
    default void afterInvoke(RequestHandleContext context) {

    }

}
