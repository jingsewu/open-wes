package org.openwes.station.infrastructure.remote;

import org.openwes.wes.api.stock.IStockApi;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

@Component
public class StockService {

    @DubboReference
    private IStockApi iStockApi;

}
