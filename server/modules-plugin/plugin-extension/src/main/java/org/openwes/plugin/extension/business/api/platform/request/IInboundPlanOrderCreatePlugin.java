package org.openwes.plugin.extension.business.api.platform.request;

import org.openwes.api.platform.api.constants.ApiTypeEnum;
import org.openwes.plugin.extension.IPlugin;

public interface IInboundPlanOrderCreatePlugin extends IPlugin, IRequestPlugin {

    /**
     * the API type
     *
     * @return
     * @see org.openwes.api.platform.api.constants.ApiTypeEnum
     */
    default String getApiType() {
        return ApiTypeEnum.ORDER_INBOUND_CREATE.name();
    }

}
