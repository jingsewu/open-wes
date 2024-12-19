package org.openwes.wes.api.outbound;

import jakarta.validation.Valid;
import org.openwes.wes.api.outbound.dto.EmptyContainerOutboundOrderCreateDTO;

import java.util.List;

public interface IEmptyContainerOutboundOrderApi {

    void createEmptyContainerOutboundOrder(@Valid EmptyContainerOutboundOrderCreateDTO emptyContainerOutboundOrderCreateDTO);

    void execute(List<Long> orderIds);

    void cancel(List<Long> orderIds);
}
