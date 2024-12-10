package org.openwes.wes.outbound.domain.repository;

import org.openwes.wes.outbound.domain.entity.PickingOrder;

import java.util.Collection;
import java.util.List;

public interface PickingOrderRepository {

    void saveOrderAndDetail(PickingOrder pickingOrder);

    void saveAllOrders(List<PickingOrder> pickingOrders);

    List<PickingOrder> saveOrderAndDetails(List<PickingOrder> pickingOrders);

    List<PickingOrder> findOrderByPickingOrderIds(Collection<Long> pickingOrderIds);

    List<PickingOrder> findOrderAndDetailsByPickingOrderIds(Collection<Long> pickingOrderIds);

    List<PickingOrder> findOrderAndDetailsByPickingOrderIdsAndDetailIds(Collection<Long> pickingOrderIds, Collection<Long> detailIds);

    PickingOrder findById(Long pickingOrderId);

    List<PickingOrder> findByWaveNos(Collection<String> waveNos);

    List<PickingOrder> findOrderAndDetailsByWaveNos(Collection<String> waveNos);

    List<PickingOrder> findByWaveNo(String waveNo);

    List<PickingOrder> findWavePickingOrderById(Long pickingOrderId);

    List<PickingOrder> findAllByPickingDetailIds(List<Long> pickingOrderDetailIds);
}
