package org.openwes.wes.api.algo;

import com.google.common.collect.Maps;
import org.openwes.wes.api.basic.constants.WarehouseAreaWorkTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class PickingOrderAlgoApiFactory implements InitializingBean {

    private static final Map<WarehouseAreaWorkTypeEnum, IPickingOrderAlgoApi> pickingOrderAlgoApiMap = Maps.newHashMap();

    private final List<IPickingOrderAlgoApi> pickingOrderAlgoApis;

    @Autowired
    public PickingOrderAlgoApiFactory(List<IPickingOrderAlgoApi> pickingOrderAlgoApis) {
        this.pickingOrderAlgoApis = pickingOrderAlgoApis;
    }

    @Override
    public void afterPropertiesSet() {
        if (CollectionUtils.isEmpty(pickingOrderAlgoApis)) {
            log.debug("cannot find any picking order algo api implements");
            return;
        }
        pickingOrderAlgoApis.forEach(api -> pickingOrderAlgoApiMap.put(api.getWarehouseAreaWorkType(), api));
    }

    public IPickingOrderAlgoApi getPickingOrderAlgoApi(WarehouseAreaWorkTypeEnum workType) {
        return pickingOrderAlgoApiMap.get(workType);
    }

}
