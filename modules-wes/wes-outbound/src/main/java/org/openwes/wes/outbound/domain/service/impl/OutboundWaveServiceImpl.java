package org.openwes.wes.outbound.domain.service.impl;

import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;
import org.openwes.wes.outbound.domain.service.OutboundWaveService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OutboundWaveServiceImpl implements OutboundWaveService {

    @Override
    public Collection<List<OutboundPlanOrder>> wavePickings(List<OutboundPlanOrder> outboundPlanOrders) {

        // 外部波次号为空的订单单组独成为一个波次
        List<List<OutboundPlanOrder>> emptyWaveNoOrders = outboundPlanOrders.stream()
                .filter(order -> StringUtils.isEmpty(order.getCustomerWaveNo()))
                .map(List::of).toList();

        // 有波次号的订单按照波次号组波
        Map<String, List<OutboundPlanOrder>> outboundWaveMap = outboundPlanOrders.stream()
                .filter(order -> StringUtils.isNotEmpty(order.getCustomerWaveNo()))
                .collect(Collectors.groupingBy(OutboundPlanOrder::getCustomerWaveNo));

        return CollectionUtils.union(emptyWaveNoOrders, outboundWaveMap.values());
    }
}
