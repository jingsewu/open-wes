package org.openwes.wes.outbound.domain.service;

import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderCancelDTO;
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderDTO;
import org.openwes.wes.common.validator.ValidateResult;
import org.openwes.wes.outbound.domain.context.OutboundPlanOrderCancelContext;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;

import java.util.Set;

public interface OutboundPlanOrderService {

    void beforeDoCreation(OutboundPlanOrderDTO outboundPlanOrderDTO);

    ValidateResult<Set<SkuMainDataDTO>> validate(OutboundPlanOrder outboundPlanOrder);

    void afterDoCreation(OutboundPlanOrder outboundPlanOrder);

    void syncValidate(OutboundPlanOrder outboundPlanOrder);

    OutboundPlanOrderCancelContext prepareCancelContext(OutboundPlanOrderCancelDTO outboundPlanOrderCancelDTO);

}
