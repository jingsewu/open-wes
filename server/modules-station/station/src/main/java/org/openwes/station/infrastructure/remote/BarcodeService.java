package org.openwes.station.infrastructure.remote;

import org.openwes.wes.api.config.IBarcodeParseRuleApi;
import org.openwes.wes.api.config.dto.BarcodeParseRequestDTO;
import org.apache.dubbo.config.annotation.DubboReference;
import org.openwes.wes.api.config.dto.BarcodeParseResult;
import org.springframework.stereotype.Service;

@Service
public class BarcodeService {

    @DubboReference
    private IBarcodeParseRuleApi barcodeParseRuleApi;

    public BarcodeParseResult parse(BarcodeParseRequestDTO requestDTO) {
        return barcodeParseRuleApi.parse(requestDTO);
    }
}
