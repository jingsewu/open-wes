package org.openwes.wes.basic.main.data.domain.service;

import org.openwes.wes.basic.main.data.domain.entity.SkuMainData;

import java.util.List;

public interface SkuMainDataService {

    void validate(List<SkuMainData> skuMainData);
}
