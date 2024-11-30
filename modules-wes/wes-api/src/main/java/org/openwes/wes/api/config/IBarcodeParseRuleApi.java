package org.openwes.wes.api.config;

import org.openwes.wes.api.config.dto.BarcodeParseRequestDTO;
import org.openwes.wes.api.config.dto.BarcodeParseResult;
import org.openwes.wes.api.config.dto.BarcodeParseRuleDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public interface IBarcodeParseRuleApi {

    void save(@Valid BarcodeParseRuleDTO barcodeParseRuleDTO);

    void update(@Valid BarcodeParseRuleDTO barcodeParseRuleDTO);

    List<BarcodeParseResult> parse(@Valid BarcodeParseRequestDTO barcodeParseRequestDTO);

    void enable(@NotNull Long barcodeParseRuleId);

    void disable(@NotNull Long barcodeParseRuleId);
}
