package org.openwes.wes.basic.work_station.application.event;

import com.google.common.eventbus.Subscribe;
import org.openwes.wes.api.basic.event.AssignOrderEvent;
import org.openwes.wes.api.basic.event.RemindSealContainerEvent;
import org.openwes.wes.common.facade.StationCallbackFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutWallSubscribe {

    private final StationCallbackFacade stationCallbackFacade;

    @Subscribe
    public void onAssignOrderEvent(@Valid AssignOrderEvent assignOrderEvent) {
        stationCallbackFacade.assignOrder(assignOrderEvent.getDetails());
    }

    @Subscribe
    public void onRemindSealContainerEvent(@Valid RemindSealContainerEvent remindSealContainerEvent) {
        stationCallbackFacade.remindSealContainer(remindSealContainerEvent.getDetails());
    }
}
