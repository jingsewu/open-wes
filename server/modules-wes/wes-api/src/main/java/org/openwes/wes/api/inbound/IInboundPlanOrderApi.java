package org.openwes.wes.api.inbound;

import org.openwes.wes.api.inbound.constants.InboundPlanOrderStatusEnum;
import org.openwes.wes.api.inbound.dto.AcceptRecordDTO;
import org.openwes.wes.api.inbound.dto.InboundPlanOrderDTO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface IInboundPlanOrderApi {

    void createInboundPlanOrder(@Valid List<InboundPlanOrderDTO> inboundPlanOrderDTOS);

    void accept(AcceptRecordDTO acceptRecordDTO);

    void forceCompleteAccept(Long inboundPlanOrderId);

    Collection<String> cancel(Collection<String> identifyNos, String warehouseCode);

    Collection<String> close(Set<Long> inboundPlanOrderIds);

    InboundPlanOrderDTO queryByLpnCodeOrCustomerOrderNoAndThrowException(String identifyNo, String warehouseCode);

    InboundPlanOrderDTO queryByLpnCodeOrCustomerOrderNo(String identifyNo, String warehouseCode);

    InboundPlanOrderDTO queryInboundPlanOrderById(Long id);

    List<InboundPlanOrderDTO> queryInboundOrderByStationId(Long workStationId);

    List<InboundPlanOrderDTO> queryByStatus(Collection<InboundPlanOrderStatusEnum> inboundPlanOrderStatusEnums);
}
