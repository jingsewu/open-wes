package org.openwes.wes.inbound.domain.repository;

import org.openwes.wes.inbound.domain.entity.PutAwayTask;

import java.util.Collection;
import java.util.List;

public interface PutAwayTaskRepository {

    PutAwayTask saveOrderAndDetail(PutAwayTask putAwayTask);

    List<PutAwayTask> saveAllOrdersAndDetails(List<PutAwayTask> putAwayTasks);

    List<PutAwayTask> findAllByTaskNoIn(Collection<String> taskNos);

    void saveAllOrders(List<PutAwayTask> putAwayTasks);

    List<PutAwayTask> findAllByIds(Collection<Long> putAwayTaskIds);
}
