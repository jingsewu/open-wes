package org.openwes.api.platform.api;

import org.openwes.api.platform.api.dto.request.RequestHandleContext;

public interface IRequestExtensionApi {

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
    void validate(RequestHandleContext context);

    /**
     * supply the context or do something before invoke
     *
     * @param context
     */
    void supply(RequestHandleContext context);

    /**
     * invoke the api, frequently call wes api in this function
     *
     * @param context
     */
    void invoke(RequestHandleContext context);

    /**
     * do something after invoke
     *
     * @param context
     */
    void afterInvoke(RequestHandleContext context);
}
