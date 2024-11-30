package org.openwes.wes.api.stock;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public interface IStockAdjustmentApi {
    void adjust(@NotEmpty List<Long> ids);

    void close(@NotEmpty List<Long> ids);

}
