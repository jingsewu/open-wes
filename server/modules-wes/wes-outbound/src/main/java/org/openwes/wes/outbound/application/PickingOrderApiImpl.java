package org.openwes.wes.outbound.application;

import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.wes.api.algo.dto.PickingOrderReallocateContext;
import org.openwes.wes.api.algo.dto.PickingOrderReallocatedResult;
import org.openwes.wes.api.outbound.IPickingOrderApi;
import org.openwes.wes.api.outbound.dto.PickingOrderDTO;
import org.openwes.wes.outbound.domain.aggregate.PickingOrderTaskAggregate;
import org.openwes.wes.outbound.domain.entity.PickingOrder;
import org.openwes.wes.outbound.domain.entity.PickingOrderDetail;
import org.openwes.wes.outbound.domain.repository.PickingOrderRepository;
import org.openwes.wes.outbound.domain.service.PickingOrderService;
import org.openwes.wes.outbound.domain.transfer.PickingOrderTransfer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

@Service
@Primary
@Validated
@DubboService
@RequiredArgsConstructor
@Slf4j
public class PickingOrderApiImpl implements IPickingOrderApi {

    private final PickingOrderRepository pickingOrderRepository;
    private final PickingOrderTransfer pickingOrderTransfer;
    private final PickingOrderTaskAggregate pickingOrderTaskAggregate;
    private final RedisUtils redisUtils;
    private final PickingOrderService pickingOrderService;

    @Override
    public List<PickingOrderDTO> getPickingOrderByWaveNo(String waveNo) {
        List<PickingOrder> pickingOrders = pickingOrderRepository.findByWaveNo(waveNo);
        return pickingOrderTransfer.toDTOs(pickingOrders);
    }

    @Override
    public List<PickingOrderDTO> getWavePickingOrderById(Long pickingOrderId) {
        return pickingOrderTransfer.toDTOs(pickingOrderRepository.findWavePickingOrderById(pickingOrderId));
    }

    @Override
    public List<PickingOrderDTO> getOrderAndDetailByPickingOrderIdsAndDetailIds(Collection<Long> pickingOrderIds, Collection<Long> detailIds) {
        return pickingOrderTransfer.toDTOs(pickingOrderRepository.findOrderAndDetailsByPickingOrderIdsAndDetailIds(pickingOrderIds, detailIds));
    }

    @Override
    public PickingOrderDTO getById(Long pickingOrderId) {
        return pickingOrderTransfer.toDTO(pickingOrderRepository.findById(pickingOrderId));
    }

    @Override
    public void receive(List<Long> pickingOrderIds, String receivedUserAccount) {
        List<PickingOrder> pickingOrders = pickingOrderRepository.findOrderByPickingOrderIds(pickingOrderIds);
        pickingOrders.forEach(v -> v.receive(receivedUserAccount));
        pickingOrderRepository.saveAllOrders(pickingOrders);
    }

    @Override
    public void allowReceive(List<Long> pickingOrderIds) {
        List<PickingOrder> pickingOrders = pickingOrderRepository.findOrderByPickingOrderIds(pickingOrderIds);
        pickingOrders.forEach(PickingOrder::allowReceive);
        pickingOrderRepository.saveAllOrders(pickingOrders);
    }

    @Override
    public void reallocate(List<Long> pickingOrderDetailIds) {
        List<PickingOrder> pickingOrders = pickingOrderRepository.findAllByPickingDetailIds(pickingOrderDetailIds);

        if (CollectionUtils.isEmpty(pickingOrders)) {
            log.debug("can not find abnormal orders with picking order detail ids: {}, maybe short completed", pickingOrderDetailIds);
            redisUtils.removeList(RedisConstants.PICKING_ORDER_ABNORMAL_DETAIL_IDS, pickingOrderDetailIds);
            return;
        }

        // remove abnormal qty is zero detail ids from redis
        List<Long> normalDetailIds = pickingOrders.stream().flatMap(v -> v.getDetails().stream())
                .filter(v -> v.getQtyAbnormal() <= 0).map(PickingOrderDetail::getId).toList();

        redisUtils.removeList(RedisConstants.PICKING_ORDER_ABNORMAL_DETAIL_IDS, normalDetailIds);

        pickingOrders.forEach(pickingOrder -> {
            String warehouseCode = pickingOrder.getWarehouseCode();
            PickingOrderReallocateContext pickingOrderReallocateContext = pickingOrderService.prepareReallocateStockContext(warehouseCode, pickingOrder);

            // if the picking order is not allowed short outbound and the available stock is not enough, then we should not allocate stocks
            PickingOrderReallocatedResult pickingOrderReallocatedResult = pickingOrderService.reallocateStocks(pickingOrderReallocateContext);

            pickingOrderTaskAggregate.reallocate(pickingOrderReallocatedResult, pickingOrder);
        });

    }
}
