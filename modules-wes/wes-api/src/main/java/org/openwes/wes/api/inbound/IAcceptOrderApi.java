package org.openwes.wes.api.inbound;

import jakarta.validation.constraints.NotNull;

public interface IAcceptOrderApi {

    void complete(@NotNull Long acceptOrderId);

    void cancel(@NotNull Long acceptOrderId, Long acceptOrderDetailId);

    void complete(String containerCode);
}
