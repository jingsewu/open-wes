package org.openwes.api.platform.application.service.handler.request.station;

import org.openwes.api.platform.api.constants.ApiTypeEnum;
import org.openwes.api.platform.application.context.RequestHandleContext;
import org.openwes.api.platform.application.service.handler.RequestHandler;
import org.openwes.api.platform.infrastructure.WorkStationClientService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TagPtlRequestHandler extends RequestHandler {
    private final WorkStationClientService workStationClientService;

    @Override
    public void invoke(RequestHandleContext context) {

        List<TapPtlEvent> list = getTargetList(context, TapPtlEvent.class);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(workStationClientService::tapPtl);
    }

    @Override
    public String getApiType() {
        return ApiTypeEnum.PTL_REPORT.name();
    }


    @Data
    public static class TapPtlEvent {
        @NotEmpty
        private String ptlTag;
        @NotNull
        private Long workStationId;
    }
}
