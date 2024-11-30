package org.openwes.wes.inbound.domain.repository;

import org.openwes.wes.inbound.domain.entity.AcceptOrder;
import org.openwes.wes.inbound.domain.entity.AcceptOrderDetail;

import java.util.Collection;
import java.util.List;

public interface AcceptOrderRepository {

    void saveOrder(AcceptOrder acceptOrder);

    void saveOrderAndDetail(AcceptOrder acceptOrder);

    List<AcceptOrder> findAllByInboundPlanOrderIds(Collection<Long> inboundPlanOrderIds);

    List<AcceptOrderDetail> findAllDetailsByStationId(Long workStationId);

    AcceptOrder findById(Long acceptOrderId);

    List<AcceptOrder> findAllByIdentifyNo(String identifyNo);

    AcceptOrder findNewStatusAcceptOrder(String identifyNo);

}
