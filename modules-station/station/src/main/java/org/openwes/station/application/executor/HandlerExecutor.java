package org.openwes.station.application.executor;

import org.openwes.station.api.constants.ApiCodeEnum;
import jakarta.validation.constraints.NotNull;

public interface HandlerExecutor {

    void execute(@NotNull ApiCodeEnum apiCode, String body, @NotNull Long workStationId);

}
