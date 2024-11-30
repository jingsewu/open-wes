package org.openwes.wes.task.domain.repository;

import org.openwes.wes.task.domain.entity.TransferContainer;

import java.util.Collection;
import java.util.List;

public interface TransferContainerRepository {

    void save(TransferContainer transferContainer);

    void saveAll(List<TransferContainer> transferContainers);

    TransferContainer findByContainerCodeAndWarehouseCode(String containerCode, String warehouseCode);

    List<TransferContainer> findAllByWarehouseCodeAndContainerCodeIn(String warehouseCode, Collection<String> containerCodes);

    List<TransferContainer> findAllLockedContainers(int limitDays);
}
