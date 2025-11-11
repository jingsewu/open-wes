package org.openwes.wes.outbound.domain.aggregate;

import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.wes.outbound.domain.entity.OutboundWave;
import org.openwes.wes.outbound.domain.entity.PickingOrder;
import org.openwes.wes.outbound.domain.repository.OutboundWaveRepository;
import org.openwes.wes.outbound.domain.repository.PickingOrderRepository;
import org.openwes.wes.outbound.domain.service.OutboundWaveService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.openwes.common.utils.constants.RedisConstants.NEW_PICKING_ORDER_IDS;

@Service
@RequiredArgsConstructor
public class PickingOrderWaveAggregate {

    private final OutboundWaveService outboundWaveService;
    private final PickingOrderRepository pickingOrderRepository;
    private final OutboundWaveRepository outboundWaveRepository;
    private final RedisUtils redisUtils;

    @Transactional(rollbackFor = Exception.class)
    public void split(OutboundWave outboundWave) {
        List<PickingOrder> pickingOrders = outboundWaveService.spiltWave(outboundWave);
        List<PickingOrder> savePickingOrders = pickingOrderRepository.saveAllOrderAndDetails(pickingOrders);

        outboundWave.process();
        outboundWaveRepository.save(outboundWave);

        redisUtils.pushAll(NEW_PICKING_ORDER_IDS, savePickingOrders.stream().map(PickingOrder::getId).toList());
    }
}
