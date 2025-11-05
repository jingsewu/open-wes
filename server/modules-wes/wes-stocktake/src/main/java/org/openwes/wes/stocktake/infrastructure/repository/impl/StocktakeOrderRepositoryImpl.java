package org.openwes.wes.stocktake.infrastructure.repository.impl;

import lombok.RequiredArgsConstructor;
import org.openwes.domain.event.AggregatorRoot;
import org.openwes.wes.api.stocktake.constants.StocktakeOrderStatusEnum;
import org.openwes.wes.stocktake.domain.entity.StocktakeOrder;
import org.openwes.wes.stocktake.domain.entity.StocktakeOrderDetail;
import org.openwes.wes.stocktake.domain.repository.StocktakeOrderRepository;
import org.openwes.wes.stocktake.infrastructure.persistence.mapper.StocktakeOrderDetailPORepository;
import org.openwes.wes.stocktake.infrastructure.persistence.mapper.StocktakeOrderPORepository;
import org.openwes.wes.stocktake.infrastructure.persistence.po.StocktakeOrderDetailPO;
import org.openwes.wes.stocktake.infrastructure.persistence.po.StocktakeOrderPO;
import org.openwes.wes.stocktake.infrastructure.persistence.transfer.StocktakeOrderPOTransfer;
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
    @Transactional(rollbackFor = Exception.class)
    public void saveAllOrderAndDetails(List<StocktakeOrder> stocktakeOrderList) {
        stocktakeOrderList.forEach(AggregatorRoot::sendAndClearEvents);

        stocktakeOrderPORepository.saveAll(stocktakeOrderPOTransfer.toPOS(stocktakeOrderList));

        List<StocktakeOrderDetail> details = stocktakeOrderList.stream()
                .flatMap(stocktakeOrder -> stocktakeOrder.getDetails().stream())
                .toList();
        stocktakeOrderDetailPORepository.saveAll(stocktakeOrderPOTransfer.toDetailPOS(details));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveStocktakeOrder(StocktakeOrder stocktakeOrder) {

        stocktakeOrder.sendAndClearEvents();

        stocktakeOrderPORepository.save(stocktakeOrderPOTransfer.toPO(stocktakeOrder));
        List<StocktakeOrderDetailPO> stocktakeOrderDetailPOS = stocktakeOrderPOTransfer.toDetailPOS(stocktakeOrder.getDetails());
        stocktakeOrderDetailPORepository.saveAll(stocktakeOrderDetailPOS);
    }

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
    public StocktakeOrder findById(Long id) {
        StocktakeOrderPO stocktakeOrderPO = stocktakeOrderPORepository.findById(id).orElseThrow();
        List<StocktakeOrderDetailPO> stocktakeOrderDetailPOs = stocktakeOrderDetailPORepository.findAllByStocktakeOrderId(stocktakeOrderPO.getId());
        return stocktakeOrderPOTransfer.toDO(stocktakeOrderPO, stocktakeOrderDetailPOs);
    }

}
