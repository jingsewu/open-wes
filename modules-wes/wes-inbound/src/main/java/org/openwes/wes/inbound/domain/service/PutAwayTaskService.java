package org.openwes.wes.inbound.domain.service;

import org.openwes.wes.inbound.domain.entity.PutAwayTask;

import java.util.List;

public interface PutAwayTaskService {

    void validateCreation(List<PutAwayTask> putAwayTasks);

    void calculateDirection(List<PutAwayTask> putAwayTasks);
}
