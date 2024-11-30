package org.openwes.wes.inbound.domain.service;

import org.openwes.wes.api.inbound.dto.AcceptRecordDTO;
import org.openwes.wes.api.inbound.dto.InboundPlanOrderDTO;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.common.validator.ValidateResult;
import org.openwes.wes.inbound.domain.entity.InboundPlanOrder;

import java.util.List;
import java.util.Set;

public interface InboundPlanOrderService {

    void beforeDoCreation(List<InboundPlanOrderDTO> inboundPlanOrderDTO);

    ValidateResult<Set<SkuMainDataDTO>> validateCreation(List<InboundPlanOrder> inboundPlanOrder);

    void afterDoCreation(List<InboundPlanOrder> inboundPlanOrder);

    void syncValidate(List<InboundPlanOrder> inboundPlanOrder);

    void validateAccept(AcceptRecordDTO acceptRecord, Long skuId);

    void validateClose(List<InboundPlanOrder> inboundPlanOrders);

    void validateForceCompleteAccept(InboundPlanOrder inboundPlanOrder);
}
