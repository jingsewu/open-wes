package org.openwes.station.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.StationErrorDescEnum;
import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.api.dto.TapPtlEvent;
import org.openwes.station.application.business.handler.event.outbound.ReportAbnormalEvent;
import org.openwes.station.application.business.handler.event.outbound.SplitTasksEvent;
import org.openwes.station.application.business.handler.event.outbound.TapPutWallSlotEvent;
import org.openwes.station.application.executor.HandlerExecutor;
import org.openwes.station.controller.view.ViewHelper;
import org.openwes.station.domain.entity.WorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.infrastructure.filters.HttpStationContext;
import org.openwes.wes.api.ems.proxy.dto.ContainerArrivedEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * receive operator's operations
 */
@RestController
@RequestMapping("api")
@Validated
@Tag(name = "Station Module Api")
@RequiredArgsConstructor
public class StationApiController {

    private final HandlerExecutor handlerExecutor;
    private final ViewHelper viewHelper;
    private final WorkStationCacheRepository workStationCacheRepository;

    @PutMapping
    public void execute(@RequestParam @Parameter(explode = Explode.TRUE) ApiCodeEnum apiCode,
                        @RequestBody(required = false)
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                content = @Content(
                                        schema = @Schema(
                                                oneOf = {
                                                        ContainerArrivedEvent.class,
                                                        SplitTasksEvent.class,
                                                        ReportAbnormalEvent.class,
                                                        TapPutWallSlotEvent.class,
                                                        TapPtlEvent.class
                                                })
                                )
                        ) String body) {
        Long workStationId = HttpStationContext.getWorkStationId();
        if (workStationId == null) {
            throw WmsException.throwWmsException(StationErrorDescEnum.STATION_ID_IS_NOT_CONFIGURED);
        }
        handlerExecutor.execute(apiCode, body, workStationId);
    }

    @GetMapping
    public Object getWorkStationVO() {
        Long workStationId = HttpStationContext.getWorkStationId();
        if (workStationId == null) {
            throw WmsException.throwWmsException(StationErrorDescEnum.STATION_ID_IS_NOT_CONFIGURED);
        }
        return viewHelper.getWorkStationVO(workStationId);
    }

    @DeleteMapping
    public void clearWorkStationCache() {
        Long workStationId = HttpStationContext.getWorkStationId();
        WorkStationCache workStationCache = workStationCacheRepository.findById(workStationId);
        workStationCacheRepository.delete(workStationCache);
    }
}
