package org.openwes.wes.printer.application.event;

import com.google.common.eventbus.Subscribe;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.openwes.wes.api.print.event.PrintEvent;
import org.openwes.wes.printer.domain.entity.PrintConfig;
import org.openwes.wes.printer.domain.entity.PrintRule;
import org.openwes.wes.printer.domain.repository.PrintConfigRepository;
import org.openwes.wes.printer.domain.repository.PrintRuleRepository;
import org.openwes.wes.printer.domain.service.PrintService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PrinterEventSubscriber {

    private final PrintConfigRepository printConfigRepository;
    private final PrintRuleRepository printRuleRepository;
    private final PrintService printService;

    @Subscribe
    public void onPrinterEvent(@Valid PrintEvent event) {

        List<PrintConfig> printConfigPOS = printConfigRepository.findAllByWorkStationId(event.getWorkStationId())
                .stream().filter(PrintConfig::isEnabled).filter(v -> CollectionUtils.isNotEmpty(v.getPrintConfigDetails()))
                .toList();
        if (CollectionUtils.isEmpty(printConfigPOS)) {
            return;
        }

        Set<PrintConfig.PrintConfigDetail> printConfigDetails = printConfigPOS.stream()
                .flatMap(v -> v.getPrintConfigDetails().stream()).collect(Collectors.toSet());

        Set<String> ruleCodes = printConfigDetails.stream().map(PrintConfig.PrintConfigDetail::getRuleCode).collect(Collectors.toSet());
        List<PrintRule> printRulePOS = printRuleRepository.findAllByRuleCodeIn(ruleCodes);

        if (printRulePOS.isEmpty()) {
            return;
        }

        printService.print(printRulePOS, printConfigDetails, event);
    }

}
