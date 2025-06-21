package org.openwes.plugin.extension.business.api.platform.request;

import org.openwes.api.platform.api.constants.ApiTypeEnum;
import org.openwes.plugin.extension.IPlugin;

public interface IOutboundPlanOrderCreatePlugin extends IPlugin, IRequestPlugin {


    /**
     * the API type
     *
     * @return
     * @see org.openwes.api.platform.api.constants.ApiTypeEnum
     */
    default String getApiType() {
        return ApiTypeEnum.ORDER_OUTBOUND_CREATE.name();
    }
}
