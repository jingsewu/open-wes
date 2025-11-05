package org.openwes.wes.api.outbound.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.openwes.domain.event.api.DomainEvent;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class OutboundWaveCompletionEvent extends DomainEvent {

    private String waveNo;

    private List<Long> outboundPlanOrderIds;

    public OutboundWaveCompletionEvent(Long id, String waveNo, List<Long> outboundPlanOrderIds) {
        super(id);
        this.waveNo = waveNo;
        this.outboundPlanOrderIds = outboundPlanOrderIds;
    }
}
