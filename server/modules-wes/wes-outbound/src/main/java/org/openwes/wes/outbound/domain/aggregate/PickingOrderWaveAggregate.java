package org.openwes.wes.outbound.domain.aggregate;

import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.wes.outbound.domain.entity.OutboundWave;
import org.openwes.wes.outbound.domain.entity.PickingOrder;
import org.openwes.wes.outbound.domain.repository.OutboundWaveRepository;
import org.openwes.wes.outbound.domain.repository.PickingOrderRepository;
import org.openwes.wes.outbound.domain.service.PickingOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.openwes.common.utils.constants.RedisConstants.NEW_PICKING_ORDER_IDS;

@Service
@RequiredArgsConstructor
public class PickingOrderWaveAggregate {

    private final PickingOrderService outboundPickingOrderService;
    private final PickingOrderRepository pickingOrderRepository;
    private final OutboundWaveRepository outboundWaveRepository;
    private final RedisUtils redisUtils;

    @Transactional(rollbackFor = Exception.class)
    public void split(OutboundWave outboundWave) {
        List<PickingOrder> pickingOrders = outboundPickingOrderService.spiltWave(outboundWave);
        List<PickingOrder> savePickingOrders = pickingOrderRepository.saveOrderAndDetails(pickingOrders);

        outboundWave.process();
        outboundWaveRepository.save(outboundWave);

        Map<String, List<PickingOrder>> warehousePickingOrderMap = savePickingOrders.stream().collect(Collectors.groupingBy(PickingOrder::getWarehouseCode));

        warehousePickingOrderMap.forEach((warehouseCode, subPickingOrders) -> {
            String redisKey = NEW_PICKING_ORDER_IDS + "_" + warehouseCode;
            redisUtils.pushAll(redisKey, subPickingOrders.stream().map(PickingOrder::getId).toList());
        });

    }
}
