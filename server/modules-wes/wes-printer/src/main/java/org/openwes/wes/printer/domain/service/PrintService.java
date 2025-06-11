package org.openwes.wes.printer.domain.service;

import jakarta.validation.Valid;
import org.openwes.wes.api.print.event.PrintEvent;
import org.openwes.wes.printer.domain.entity.PrintConfig;
import org.openwes.wes.printer.domain.entity.PrintRule;

import java.util.List;
import java.util.Set;

public interface PrintService {
    void print(List<PrintRule> printRulePOS,
               Set<PrintConfig.PrintConfigDetail> printConfigDetails, @Valid PrintEvent event);
}
