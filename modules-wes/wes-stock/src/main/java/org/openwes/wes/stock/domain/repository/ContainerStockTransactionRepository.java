package org.openwes.wes.stock.domain.repository;

import org.openwes.wes.stock.domain.entity.ContainerStockTransaction;

import java.util.List;

public interface ContainerStockTransactionRepository {

    ContainerStockTransaction save(ContainerStockTransaction transactionRecord);

    ContainerStockTransaction findByTurnOverContainerCode(String turnOverContainerCode);

    void saveAll(List<ContainerStockTransaction> containerStockTransactions);
}
