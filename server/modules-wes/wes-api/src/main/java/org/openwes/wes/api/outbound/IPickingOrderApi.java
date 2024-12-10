package org.openwes.wes.api.outbound;


import org.openwes.wes.api.outbound.dto.PickingOrderDTO;

import java.util.Collection;
import java.util.List;

public interface IPickingOrderApi {

    PickingOrderDTO getById(Long pickingOrderId);

    List<PickingOrderDTO> getOrderAndDetailByPickingOrderIdsAndDetailIds(Collection<Long> pickingOrderIds, Collection<Long> detailIds);

    List<PickingOrderDTO> getPickingOrderByWaveNo(String waveNo);

    List<PickingOrderDTO> getWavePickingOrderById(Long pickingOrderId);

    void receive(List<Long> pickingOrderIds, String userAccount);

    void allowReceive(List<Long> pickingOrderIds);

    void reallocate(List<Long> pickingOrderDetailIds);
}
