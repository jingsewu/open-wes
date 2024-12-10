package org.openwes.wes.stock.domain.service;

import org.openwes.wes.api.stock.dto.StockTransferDTO;
import org.openwes.wes.stock.domain.entity.ContainerStock;
import org.openwes.wes.stock.domain.entity.SkuBatchStock;

public interface StockService {

    ContainerStock transferContainerStock(StockTransferDTO stockTransferDTO, ContainerStock containerStock, Long targetSkuBatchId, boolean unlock);

    SkuBatchStock transferSkuBatchStock(SkuBatchStock skuBatchStock, StockTransferDTO stockTransferDTO, boolean unlock);

}
