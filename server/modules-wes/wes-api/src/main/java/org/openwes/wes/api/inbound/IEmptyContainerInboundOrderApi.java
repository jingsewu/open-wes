package org.openwes.wes.api.inbound;

import org.openwes.wes.api.inbound.constants.EmptyContainerInboundWayEnum;
import org.openwes.wes.api.inbound.dto.EmptyContainerInboundRecordDTO;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Collection;
import java.util.List;

public interface IEmptyContainerInboundOrderApi {

    void create(@NotEmpty String warehouseCode, @NotNull EmptyContainerInboundWayEnum inboundWay,
                @NotEmpty List<EmptyContainerInboundRecordDTO> emptyContainerInboundRecordDTOS);

    void completeDetails(Collection<Long> detailIds);
}
