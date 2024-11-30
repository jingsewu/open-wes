package org.openwes.wes.inbound.domain.repository;

import org.openwes.wes.api.inbound.constants.InboundPlanOrderStatusEnum;
import org.openwes.wes.inbound.domain.entity.InboundPlanOrder;

import java.util.Collection;
import java.util.List;
import java.util.Set;


public interface InboundPlanOrderRepository {

    void saveOrders(Collection<InboundPlanOrder> inboundPlanOrders);

    InboundPlanOrder saveOrderAndDetail(InboundPlanOrder inboundPlanOrder);

    List<InboundPlanOrder> saveAllOrdersAndDetails(List<InboundPlanOrder> inboundPlanOrders);

    InboundPlanOrder findById(Long inboundPlanOrderId);

    List<InboundPlanOrder> findAllByLpnCodeOrCustomerOrderNoAndWarehouseCode(String identifyNo, String warehouseCode);

    List<InboundPlanOrder> findAllByLpnCodeOrCustomerOrderNoAndWarehouseCode(Collection<String> identifyNos, String warehouseCode);

    boolean existByCustomerOrderNo(Collection<String> customerOrderNos, String warehouseCode);

    boolean existByBoxNos(Collection<String> boxNos, String warehouseCode);

    List<InboundPlanOrder> findAllByIds(Collection<Long> ids);

    Collection<InboundPlanOrder> findAllByStatus(Collection<InboundPlanOrderStatusEnum> inboundPlanOrderStatusEnums);

    List<InboundPlanOrder> findAllByDetailIds(Collection<Long> detailIds);

    boolean existByLpnCodeAndStatus(Set<String> lpnCodes, Collection<InboundPlanOrderStatusEnum> inboundPlanOrderStatusEnums, String warehouseCode);
}
