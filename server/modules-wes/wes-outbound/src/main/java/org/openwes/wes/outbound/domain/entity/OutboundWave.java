package org.openwes.wes.outbound.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.common.utils.id.SnowflakeUtils;
import org.openwes.domain.event.AggregatorRoot;
import org.openwes.wes.api.outbound.constants.OutboundWaveStatusEnum;
import org.openwes.wes.api.outbound.event.OutboundWaveCompletionEvent;
import org.openwes.wes.api.outbound.event.OutboundWaveCreatedEvent;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Slf4j
public class OutboundWave extends AggregatorRoot {

    private Long id;
    private String warehouseCode;
    private int priority;
    private boolean shortOutbound;
    private String waveNo;
    private List<Long> outboundPlanOrderIds;
    private OutboundWaveStatusEnum waveStatus;
    private Long version;

    public OutboundWave(String waveNo, Integer maxPriority, List<OutboundPlanOrder> orders) {
        this.id = SnowflakeUtils.generateId();
        this.waveNo = waveNo;
        this.priority = maxPriority;
        this.shortOutbound = orders.iterator().next().isShortOutbound();
        this.warehouseCode = orders.iterator().next().getWarehouseCode();
        this.outboundPlanOrderIds = orders.stream().map(OutboundPlanOrder::getId).toList();
        this.waveStatus = OutboundWaveStatusEnum.NEW;

        log.info("outbound wave id: {} waveNo: {} new.", this.id, this.waveNo);
        this.addAsynchronousDomainEvents(new OutboundWaveCreatedEvent(this.id, this.waveNo));
    }

    public void process() {

        log.info("outbound wave id: {} waveNo: {} process.", this.id, this.waveNo);

        if (this.waveStatus != OutboundWaveStatusEnum.NEW) {
            throw new IllegalStateException("outbound wave status is not NEW, can't be processed");
        }
        this.waveStatus = OutboundWaveStatusEnum.PROCESSING;
    }

    public void complete() {

        log.info("outbound wave id: {} waveNo: {} complete.", this.id, this.waveNo);

        if (this.waveStatus == OutboundWaveStatusEnum.DONE) {
            throw new IllegalStateException("outbound wave status is DONE, can't be completed");
        }
        this.waveStatus = OutboundWaveStatusEnum.DONE;

        this.addAsynchronousDomainEvents(new OutboundWaveCompletionEvent(this.id, this.waveNo, this.outboundPlanOrderIds));
    }

    public void cancel() {

        log.info("outbound wave id: {} waveNo: {} cancel.", this.id, this.waveNo);

        if (this.waveStatus == OutboundWaveStatusEnum.DONE) {
            throw new IllegalStateException("outbound wave status is DONE, can't be canceled");
        }
        this.waveStatus = OutboundWaveStatusEnum.CANCELED;
    }
}
