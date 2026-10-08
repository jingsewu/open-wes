package org.openwes.wes.outbound.domain.service.impl;

import org.junit.jupiter.api.Test;
import org.openwes.plugin.extension.business.wes.outbound.action.IOutboundWaveSplitAction;
import org.openwes.plugin.extension.business.wes.outbound.action.IOutboundWaveWaveAction;
import org.openwes.plugin.sdk.utils.PluginSdkUtils;
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderDTO;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;
import org.openwes.wes.outbound.domain.entity.OutboundWave;
import org.openwes.wes.outbound.domain.repository.OutboundPlanOrderRepository;
import org.openwes.wes.outbound.domain.repository.OutboundPreAllocatedRecordRepository;
import org.openwes.wes.outbound.domain.transfer.OutboundPlanOrderTransfer;
import org.openwes.wes.outbound.domain.transfer.OutboundWaveTransfer;
import org.openwes.wes.outbound.domain.transfer.PickingOrderTransfer;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboundWaveServiceImplTest {

    private final PluginSdkUtils pluginUtils = mock(PluginSdkUtils.class);
    private final OutboundPlanOrderRepository outboundPlanOrderRepository = mock(OutboundPlanOrderRepository.class);
    private final OutboundWaveTransfer outboundWaveTransfer = mock(OutboundWaveTransfer.class);
    private final PickingOrderTransfer pickingOrderTransfer = mock(PickingOrderTransfer.class);
    private final OutboundPlanOrderTransfer outboundPlanOrderTransfer = mock(OutboundPlanOrderTransfer.class);
    private final OutboundPreAllocatedRecordRepository outboundPreAllocatedRecordRepository = mock(OutboundPreAllocatedRecordRepository.class);

    private final OutboundWaveServiceImpl service = new OutboundWaveServiceImpl(pluginUtils,
            outboundPlanOrderRepository, outboundWaveTransfer, pickingOrderTransfer,
            outboundPlanOrderTransfer, outboundPreAllocatedRecordRepository);

    private static IOutboundWaveWaveAction waveAction(int priority, List<List<OutboundPlanOrderDTO>> result) {
        return new IOutboundWaveWaveAction() {
            @Override
            public int priority() {
                return priority;
            }

            @Override
            public List<List<OutboundPlanOrderDTO>> wave(List<OutboundPlanOrderDTO> outboundPlanOrderDTOs) {
                return result;
            }
        };
    }

    @Test
    void wavePickings_picksHighestPriorityAction() {
        OutboundPlanOrder order = new OutboundPlanOrder();
        order.setCustomerWaveNo("W1");

        OutboundPlanOrderDTO lowPriorityDTO = new OutboundPlanOrderDTO();
        OutboundPlanOrderDTO highPriorityDTO = new OutboundPlanOrderDTO();
        when(outboundPlanOrderTransfer.toDTOs(any())).thenReturn(List.of(lowPriorityDTO, highPriorityDTO));

        OutboundPlanOrder lowPriorityOrder = new OutboundPlanOrder();
        OutboundPlanOrder highPriorityOrder = new OutboundPlanOrder();
        when(outboundPlanOrderTransfer.toDOs(List.of(lowPriorityDTO))).thenReturn(List.of(lowPriorityOrder));
        when(outboundPlanOrderTransfer.toDOs(List.of(highPriorityDTO))).thenReturn(List.of(highPriorityOrder));

        when(pluginUtils.getExtractObject(IOutboundWaveWaveAction.class)).thenReturn(List.of(
                waveAction(10, List.of(List.of(lowPriorityDTO))),
                waveAction(100, List.of(List.of(highPriorityDTO)))));

        Collection<List<OutboundPlanOrder>> result = service.wavePickings(List.of(order));

        // the priority-100 action wins even though the priority-10 action comes first in the registry order
        assertEquals(List.of(List.of(highPriorityOrder)), result);
    }

    @Test
    void wavePickings_whenHighestPriorityReturnsNull_fallsBackToBuiltInGrouping() {
        OutboundPlanOrder orderA = new OutboundPlanOrder();
        orderA.setCustomerWaveNo("W1");
        OutboundPlanOrder orderB = new OutboundPlanOrder();
        orderB.setCustomerWaveNo("W1");
        OutboundPlanOrder orderC = new OutboundPlanOrder();
        when(pluginUtils.getExtractObject(IOutboundWaveWaveAction.class)).thenReturn(List.of(
                waveAction(100, null)));

        Collection<List<OutboundPlanOrder>> result = service.wavePickings(List.of(orderA, orderB, orderC));

        // built-in: one group for W1 (two orders) + one single-element group for the empty wave no
        assertEquals(2, result.size());
        assertEquals(1, result.stream().filter(g -> g.size() == 2).count());
        assertEquals(1, result.stream().filter(g -> g.size() == 1).count());
    }

    @Test
    void wavePickings_whenHighestPriorityThrows_fallsBackToBuiltInGrouping() {
        OutboundPlanOrder order = new OutboundPlanOrder();
        order.setCustomerWaveNo("W1");
        when(pluginUtils.getExtractObject(IOutboundWaveWaveAction.class)).thenReturn(List.of(
                new IOutboundWaveWaveAction() {
                    @Override
                    public int priority() {
                        return 100;
                    }

                    @Override
                    public List<List<OutboundPlanOrderDTO>> wave(List<OutboundPlanOrderDTO> outboundPlanOrderDTOs) {
                        throw new IllegalStateException("boom");
                    }
                }));

        Collection<List<OutboundPlanOrder>> result = service.wavePickings(List.of(order));

        assertEquals(1, result.size());
        assertEquals(1, result.iterator().next().size());
    }

    @Test
    void spiltWave_whenNoPlugins_usesBuiltInPath() {
        when(pluginUtils.getExtractObject(IOutboundWaveSplitAction.class)).thenReturn(List.of());

        service.spiltWave(new OutboundWave());

        verify(outboundPlanOrderRepository, never()).findAllByIds(any());
    }
}
