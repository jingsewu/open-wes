package org.openwes.wes.api.outbound.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "人工区拣货领用信息")
public class ReceiveManualDTO implements Serializable {

    private Long pickingOrderId;

    private List<PickingOrderDTO> pickingOrderDTOS;

    private String waveNo;

    @NotNull
    private Long receivedUserAccount;

    @NotEmpty
    private String transferContainerCode;

    // 预留字段，如果 pda 操作员想强制领取已经被其他操作员领取的任务，前端会提示是否强制领取
    private boolean forceReceive;

    private String warehouseCode;
}
