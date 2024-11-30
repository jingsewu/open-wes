package org.openwes.wes.stocktake.infrastructure.repository.impl;

import org.openwes.wes.api.stocktake.constants.StocktakeOrderStatusEnum;
import org.openwes.wes.stocktake.domain.entity.StocktakeOrder;
import org.openwes.wes.stocktake.domain.repository.StocktakeOrderRepository;
import org.openwes.wes.stocktake.infrastructure.persistence.mapper.StocktakeOrderDetailPORepository;
import org.openwes.wes.stocktake.infrastructure.persistence.mapper.StocktakeOrderPORepository;
import org.openwes.wes.stocktake.infrastructure.persistence.po.StocktakeOrderDetailPO;
import org.openwes.wes.stocktake.infrastructure.persistence.po.StocktakeOrderPO;
import org.openwes.wes.stocktake.infrastructure.persistence.transfer.StocktakeOrderPOTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StocktakeOrderRepositoryImpl implements StocktakeOrderRepository {

    private final StocktakeOrderPORepository stocktakeOrderPORepository;
    private final StocktakeOrderDetailPORepository stocktakeOrderDetailPORepository;
    private final StocktakeOrderPOTransfer stocktakeOrderPOTransfer;

    @Override
    public List<StocktakeOrder> findAllByOrderNosAndWarehouseCodeAndStatuses(Collection<String> orderNos, String warehouseCode, List<StocktakeOrderStatusEnum> statuses) {
        List<StocktakeOrderPO> stocktakeOrderPOS = stocktakeOrderPORepository
                .findAllByWarehouseCodeAndOrderNoInAndStocktakeOrderStatusIn(warehouseCode, orderNos, statuses);
        return stocktakeOrderPOS.stream().map(stocktakeOrderPO -> {
            List<StocktakeOrderDetailPO> detailPOS = stocktakeOrderDetailPORepository.findAllByStocktakeOrderId(stocktakeOrderPO.getId());
            return stocktakeOrderPOTransfer.toDO(stocktakeOrderPO, detailPOS);
        }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<StocktakeOrder> saveAllOrderAndDetails(List<StocktakeOrder> stocktakeOrderList) {
        return stocktakeOrderList.stream().map(this::saveStocktakeOrder).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StocktakeOrder saveStocktakeOrder(StocktakeOrder stocktakeOrder) {
        StocktakeOrderPO stocktakeOrderPO = stocktakeOrderPORepository.save(stocktakeOrderPOTransfer.toPO(stocktakeOrder));

        List<StocktakeOrderDetailPO> stocktakeOrderDetailPOS = stocktakeOrderPOTransfer.toDetailPOS(stocktakeOrder.getDetails());
        stocktakeOrderDetailPOS.forEach(v -> v.setStocktakeOrderId(stocktakeOrderPO.getId()));

        List<StocktakeOrderDetailPO> details = stocktakeOrderDetailPORepository.saveAll(stocktakeOrderDetailPOS);

        return stocktakeOrderPOTransfer.toDO(stocktakeOrderPO, details);
    }

    @Override
    public StocktakeOrder findById(Long id) {
        StocktakeOrderPO stocktakeOrderPO = stocktakeOrderPORepository.findById(id).orElseThrow();
        List<StocktakeOrderDetailPO> stocktakeOrderDetailPOs = stocktakeOrderDetailPORepository.findAllByStocktakeOrderId(stocktakeOrderPO.getId());
        return stocktakeOrderPOTransfer.toDO(stocktakeOrderPO, stocktakeOrderDetailPOs);
    }

}
