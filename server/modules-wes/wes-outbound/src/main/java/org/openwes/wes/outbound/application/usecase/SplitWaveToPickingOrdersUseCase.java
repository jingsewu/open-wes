package org.openwes.wes.outbound.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class SplitWaveToPickingOrdersUseCase {

    private final OutboundWaveService outboundWaveService;
    private final PickingOrderRepository pickingOrderRepository;
    private final OutboundWaveRepository outboundWaveRepository;
    private final RedisUtils redisUtils;

    @Transactional(rollbackFor = Exception.class)
    public void execute(OutboundWave outboundWave) {
        List<PickingOrder> pickingOrders = outboundWaveService.spiltWave(outboundWave);
        List<PickingOrder> savedPickingOrders = pickingOrderRepository.saveAllOrderAndDetails(pickingOrders);

        outboundWave.process();
        outboundWaveRepository.save(outboundWave);

        redisUtils.pushAll(NEW_PICKING_ORDER_IDS,
                savedPickingOrders.stream().map(PickingOrder::getId).toList());
    }
}
