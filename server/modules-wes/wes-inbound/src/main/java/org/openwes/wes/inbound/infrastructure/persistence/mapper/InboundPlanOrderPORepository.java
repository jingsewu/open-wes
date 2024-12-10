package org.openwes.wes.inbound.infrastructure.persistence.mapper;

import org.openwes.wes.api.inbound.constants.InboundPlanOrderStatusEnum;
import org.openwes.wes.inbound.infrastructure.persistence.po.InboundPlanOrderPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface InboundPlanOrderPORepository extends JpaRepository<InboundPlanOrderPO, Long> {

    List<InboundPlanOrderPO> findAllByLpnCodeInOrCustomerOrderNoInAndWarehouseCode(Collection<String> lpnCode, Collection<String> customerOrderNo, String warehouseCode);

    List<InboundPlanOrderPO> findAllByInboundPlanOrderStatusIn(Collection<InboundPlanOrderStatusEnum> statusEnums);

    List<InboundPlanOrderPO> findByCustomerOrderNoAndWarehouseCode(String customerOrderNo, String warehouseCode);

    List<InboundPlanOrderPO> findByLpnCodeAndWarehouseCode(String lpnCode, String warehouseCode);

    boolean existsByCustomerOrderNoInAndWarehouseCode(Collection<String> customerOrderNos, String warehouseCode);

    boolean existsByLpnCodeInAndInboundPlanOrderStatusInAndWarehouseCode(Set<String> lpnCodes,
                                                                         Collection<InboundPlanOrderStatusEnum> statues,
                                                                         String warehouseCode);
}
