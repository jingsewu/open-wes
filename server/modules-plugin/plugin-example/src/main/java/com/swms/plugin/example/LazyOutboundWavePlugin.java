package com.swms.plugin.example;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openwes.api.platform.api.ICallbackApi;
import org.openwes.api.platform.api.constants.CallbackApiTypeEnum;
import org.openwes.common.utils.http.Response;
import org.openwes.plugin.extension.business.wes.outbound.action.IOutboundWaveWaveAction;
import org.openwes.wes.api.outbound.IOutboundPlanOrderApi;
import org.openwes.wes.api.outbound.constants.OutboundPlanOrderStatusEnum;
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderDTO;
import org.pf4j.Extension;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Extension
@Slf4j
@RequiredArgsConstructor
public class LazyOutboundWavePlugin implements IOutboundWaveWaveAction {

    private static final String GET_PACKING_STATUS_API_CODE = "GET_PACKING_STATUS";

    // 打包台状态：在线
    private static final String PACKING_STATUS_1 = "1";

    private final IOutboundPlanOrderApi outboundPlanOrderApi;
    private final ICallbackApi callbackApi;

    @Override
    public List<List<OutboundPlanOrderDTO>> wave(List<OutboundPlanOrderDTO> originalOutboundPlanOrders) {
        log.debug("Receive lazy waving request, original outbound plan orders size: {}", originalOutboundPlanOrders.size());

        Set<String> customerWaveNos = originalOutboundPlanOrders.stream()
                .map(OutboundPlanOrderDTO::getCustomerWaveNo).collect(Collectors.toSet());
        List<OutboundPlanOrderDTO> outboundPlanOrderDTOS = outboundPlanOrderApi.getByCustomerWaveNos(customerWaveNos);

        Map<String, List<OutboundPlanOrderDTO>> outboundPlanOrderGroupMap = outboundPlanOrderDTOS.stream()
                .collect(Collectors.groupingBy(OutboundPlanOrderDTO::getCustomerWaveNo));

        // 复核打包台已上线，或者订单波次没有已完成订单，否则就过滤掉，等待打包台上线
        List<OutboundPlanOrderDTO> outboundPlanOrders = originalOutboundPlanOrders.stream()
                .filter(v -> outboundPlanOrderGroupMap.get(v.getCustomerWaveNo()).stream()
                        .noneMatch(dto -> OutboundPlanOrderStatusEnum.NEW != dto.getOutboundPlanOrderStatus()
                                && OutboundPlanOrderStatusEnum.ASSIGNED != dto.getOutboundPlanOrderStatus())
                ).toList();

        log.debug("After filter, new outbound plan orders size: {}", outboundPlanOrders.size());

        // 如果原始单量和过滤后的单量不一致，说明有父单完成了，就需要判断复核打包台的状态了
        if (originalOutboundPlanOrders.size() != outboundPlanOrders.size()) {
            boolean recheckStationIsOnline = recheckStationIsOnline();
            // 复核打包台已上线，就发全部的订单，否则只发父单没有完成的订单
            return waving(recheckStationIsOnline ? originalOutboundPlanOrders : outboundPlanOrders);
        }

        return waving(outboundPlanOrders);
    }

    private boolean recheckStationIsOnline() {
        Response result = callbackApi.callback(CallbackApiTypeEnum.COMMON_CALLBACK, GET_PACKING_STATUS_API_CODE, null);

        if (result == null || !Response.SUCCESS_CODE.equals(result.getCode())) {
            log.info("cannot get packing station status, api response is : {}", result);
            return false;
        }

        boolean isOnline = PACKING_STATUS_1.equals(result.getData());
        log.debug("Recheck station online status is : {}", isOnline);
        return isOnline;
    }

    private List<List<OutboundPlanOrderDTO>> waving(List<OutboundPlanOrderDTO> outboundPlanOrders) {
        log.debug("Waving outbound plan orders size: {}", outboundPlanOrders.size());

        // 外部波次号为空的订单单组按照指定工作站列表组成波次
        Map<String, List<OutboundPlanOrderDTO>> emptyWaveNoWaveMap = outboundPlanOrders.stream()
                .filter(order -> StringUtils.isEmpty(order.getCustomerWaveNo()))
                .collect(Collectors.groupingBy(v -> StringUtils.join(v.getCustomerOrderType(), ",")));

        // 有波次号的订单按照波次号组波
        Map<String, List<OutboundPlanOrderDTO>> outboundWaveMap = outboundPlanOrders.stream()
                .filter(order -> StringUtils.isNotEmpty(order.getCustomerWaveNo()))
                .collect(Collectors.groupingBy(OutboundPlanOrderDTO::getCustomerWaveNo));

        List<List<OutboundPlanOrderDTO>> results = Lists.newArrayList(emptyWaveNoWaveMap.values());
        results.addAll(outboundWaveMap.values());
        log.debug("Waving success! ");
        return results;
    }
}
