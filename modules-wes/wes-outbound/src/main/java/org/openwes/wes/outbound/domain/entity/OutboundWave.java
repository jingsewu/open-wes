package org.openwes.wes.outbound.domain.entity;

import org.openwes.wes.api.outbound.constants.OutboundWaveStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Data
@NoArgsConstructor
@Slf4j
public class OutboundWave {

    private Long id;
    private String warehouseCode;
    private int priority;
    private boolean shortOutbound;
    private String waveNo;
    private List<Long> outboundPlanOrderIds;
    private OutboundWaveStatusEnum waveStatus;
    private Long version;

    public OutboundWave(String waveNo, Integer maxPriority, List<OutboundPlanOrder> orders) {
        this.waveNo = waveNo;
        this.priority = maxPriority;
        this.shortOutbound = orders.iterator().next().isShortOutbound();
        this.warehouseCode = orders.iterator().next().getWarehouseCode();
        this.outboundPlanOrderIds = orders.stream().map(OutboundPlanOrder::getId).toList();
        this.waveStatus = OutboundWaveStatusEnum.NEW;
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
    }

    public void cancel() {

        log.info("outbound wave id: {} waveNo: {} cancel.", this.id, this.waveNo);

        if (this.waveStatus == OutboundWaveStatusEnum.DONE) {
            throw new IllegalStateException("outbound wave status is DONE, can't be canceled");
        }
        this.waveStatus = OutboundWaveStatusEnum.CANCELED;
    }
}
