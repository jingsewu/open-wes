package org.openwes.wes.stock.application.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.distribute.scheduler.annotation.DistributedScheduled;
import org.openwes.wes.api.config.ISystemConfigApi;
import org.openwes.wes.api.config.dto.SystemConfigDTO;
import org.openwes.wes.stock.domain.repository.ContainerStockRepository;
import org.openwes.wes.stock.domain.repository.SkuBatchStockRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockScheduler {

    private final ISystemConfigApi systemConfigApi;
    private final SkuBatchStockRepository skuBatchStockRepository;
    private final ContainerStockRepository containerStockRepository;

    @DistributedScheduled(cron = "0 1 0 * * *", name = "StockScheduler#cleanZeroQtyStocks")
    public void cleanZeroQtyStocks() {

        SystemConfigDTO.StockConfigDTO stockConfig = systemConfigApi.getStockConfig();

        int zeroStockSavedDays = stockConfig.getZeroStockSavedDays();
        long expiredTime = System.currentTimeMillis() - zeroStockSavedDays * 24L * 60 * 60 * 1000;

        log.info("clean expired time: {} zero total qty stocks.", new Date(expiredTime));

        skuBatchStockRepository.deleteAllZeroQtyStock(expiredTime);

        containerStockRepository.deleteAllZeroQtyStock(expiredTime);
    }

}
