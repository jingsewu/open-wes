package org.openwes.wes.api.inbound.dto;

import org.openwes.wes.api.inbound.constants.AcceptMethodEnum;
import org.openwes.wes.api.inbound.constants.AcceptOrderStatusEnum;
import org.openwes.wes.api.inbound.constants.AcceptTypeEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class AcceptOrderDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -3694137906829883379L;

    private Long id;

    @Size(max = 64)
    private String orderNo;

    @NotEmpty
    @Size(max = 64)
    private String warehouseCode;

    @NotNull
    private AcceptMethodEnum acceptMethod;
    @NotNull
    private AcceptTypeEnum acceptType;

    private boolean putAway;

    private Integer totalQty;
    private Integer totalBox;

    private String remark;

    private AcceptOrderStatusEnum acceptOrderStatus;

    @NotEmpty
    private List<AcceptOrderDetailDTO> details;

    private Long version;

}
