package org.openwes.wes.stock.infrastructure.persistence.mapper;

import org.openwes.wes.stock.infrastructure.persistence.po.ContainerStockTransactionPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContainerStockTransactionPORepository extends JpaRepository<ContainerStockTransactionPO, Long> {

    Optional<List<ContainerStockTransactionPO>> findAllByTargetContainerCode(String turnOverContainerCode);

}
