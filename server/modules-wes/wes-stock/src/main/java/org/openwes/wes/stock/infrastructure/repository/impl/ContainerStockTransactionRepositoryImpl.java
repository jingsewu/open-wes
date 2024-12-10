package org.openwes.wes.stock.infrastructure.repository.impl;

import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.stock.domain.entity.ContainerStockTransaction;
import org.openwes.wes.stock.domain.repository.ContainerStockTransactionRepository;
import org.openwes.wes.stock.infrastructure.persistence.mapper.ContainerStockTransactionPORepository;
import org.openwes.wes.stock.infrastructure.persistence.po.ContainerStockTransactionPO;
import org.openwes.wes.stock.infrastructure.persistence.transfer.ContainerStockTransactionPOTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContainerStockTransactionRepositoryImpl implements ContainerStockTransactionRepository {

    private final ContainerStockTransactionPORepository recordPORepository;
    private final ContainerStockTransactionPOTransfer recordPOTransfer;

    @Override
    public ContainerStockTransaction save(ContainerStockTransaction transactionRecord) {
        return recordPOTransfer.toDO(recordPORepository.save(recordPOTransfer.toPO(transactionRecord)));
    }

    @Override
    public ContainerStockTransaction findByTurnOverContainerCode(String turnOverContainerCode) {
        List<ContainerStockTransactionPO> containerStockTransactionPOS = recordPORepository.findAllByTargetContainerCode(turnOverContainerCode).orElseThrow();
        return containerStockTransactionPOS.stream()
                .max(Comparator.comparing(UpdateUserPO::getCreateTime))
                .map(recordPOTransfer::toDO)
                .orElseThrow();
    }

    @Override
    public void saveAll(List<ContainerStockTransaction> containerStockTransactions) {
        recordPORepository.saveAll(recordPOTransfer.toPOs(containerStockTransactions));
    }
}
